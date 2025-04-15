package com.jervisffb.fumbbl.web

import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerLevel
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.RosterPosition
import com.jervisffb.engine.rules.bb2020.roster.SpecialRules
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SerializedPlayer
import com.jervisffb.engine.serialize.SerializedTeam
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.fumbbl.net.utils.convertFumbblSkillToSkillId
import com.jervisffb.fumbbl.web.api.AuthResult
import com.jervisffb.fumbbl.web.api.CoachSearchResult
import com.jervisffb.fumbbl.web.api.CurrentMatchResult
import com.jervisffb.fumbbl.web.api.FumbblePlayerDetails
import com.jervisffb.fumbbl.web.api.FumbbleRosterDetails
import com.jervisffb.fumbbl.web.api.FumbbleTeamDetails
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.Json

/**
 * Wrapper around https://fumbbl.com/apidoc/
 *
 *
 * The class is intended to be coach-specific. So while the class can be created
 * without providing a coach name, it isn't possible to authenticate or use
 * authenticated methods in that case.
 *
 * This class also tracks the access token internally and will automatically
 * refresh it if needed (not currently implemented).
 */
class FumbblApi(private val coachName: String? = null, private var oauthToken: String? = null) {

    companion object {
        val LOG = jervisLogger()
        val BASE_URL = "https://fumbbl.com/api"
    }

    private val client = getHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private var authToken: AuthResult? = null
    private var coachId: Long? = null

    /**
     * Returns `true` if the client thinks it is authenticated. Note, the
     * server might think otherwise and reject the credentials.
     */
    fun isAuthenticated(): Boolean = (authToken != null && coachId != null)

    /**
     * Authenticate the client and store the access token in memory. It will be used by other endpoints if required.
     *
     * If authentication fails and [IllegalStateException] is thrown.
     */
    suspend fun authenticate(clientId: String, clientSecret: String): Result<AuthResult> = runCatching {
        checkNotNull(coachName) { "Coach name required to log in." }
        val response = client.post("$BASE_URL/oauth/token") {
            header("Authorization", "Basic ${"$clientId:$clientSecret".encodeBase64()}")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("grant_type=client_credentials")
        }
        if (response.status.isSuccess()) {
            // Looks like FUMBBL returns 200 when login fails, so some extra checks is needed here :/
            val body = response.bodyAsText()

            // Check if coach name is valid
            coachId = getCoachId(coachName)
            if (coachId == null) {
                return Result.failure(IllegalStateException("Unable to find coach id for $coachName"))
            }

            // Check if auth failed
            if (body == """{"error":"Authorization failed"}""") {
                return Result.failure(IllegalStateException("Authorization failed"))
            }

            // Otherwise we should be safe to parse the return value
            authToken = json.decodeFromString(response.bodyAsText())
            oauthToken = authToken?.accessToken
            authToken!!
        } else {
            return Result.failure(IllegalStateException("Authorization failed with status: ${response.status}"))
        }
    }

    /**
     * Returns the coach id for a given coach name.
     * Returns `null` if the coach could not be found.
     */
    suspend fun getCoachId(coachName: String): Long? {
        val result: List<CoachSearchResult> = client.get("$BASE_URL/coach/search/$coachName").body()
        return result.firstOrNull { it.name.equals(coachName, ignoreCase = true) }?.id
    }

    suspend fun getCurrentMatches(): List<CurrentMatchResult> {
        TODO()
    }

    /**
     * Load a FUMBBL Team and convert it to a Jervis, so it can be used
     * inside Jervis games.
     */
    suspend fun loadTeam(
        teamId: Long,
        rules: Rules,
    ): JervisTeamFile {
        val team = loadTeamFromFumbbl(teamId)
        val roster = loadRosterFromFumbbl(team.roster.id)
        // Unclear if we need exact player details. I assume it could be possible to override
        // the portrait, but seems fine to just use the one from the position for now
        // val players: Set<PlayerDetails> = loadTeamPlayers(team)
        val jervisRoster = convertToBB2020JervisRoster(rules, roster)
        val jervisTeam = convertToBB2020JervisTeam(rules, jervisRoster, team)
        return JervisTeamFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            team = jervisTeam,
            history = null,
        )
    }

    private suspend fun loadRosterFromFumbbl(rosterId: Int): FumbbleRosterDetails {
        val result = client.get("$BASE_URL/roster/get/$rosterId")
        if (result.status.isSuccess()) {
            return json.decodeFromString<FumbbleRosterDetails>(result.bodyAsText())
        } else {
            throw IllegalStateException("Loading roster $rosterId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamFromFumbbl(teamId: Long): FumbbleTeamDetails {
        val result = client.get("$BASE_URL/team/get/$teamId")
        if (result.status.isSuccess()) {
            return result.body<FumbbleTeamDetails>()
        } else {
            throw IllegalStateException("Loading team $teamId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamPlayers(team: FumbbleTeamDetails): Set<FumbblePlayerDetails> {
        return team.players.map { player -> loadPlayer(player.id) }.toSet()
    }

    private suspend fun loadPlayer(playerId: Int): FumbblePlayerDetails {
        val result = client.get("$BASE_URL/player/get/$playerId")
        val details = json.decodeFromString<FumbblePlayerDetails>(result.bodyAsText())
        return details
    }

    private fun mapToSkillCategory(categories: List<String>): List<SkillCategory> {
        return categories.map {
            when (it) {
                "P" -> SkillCategory.PASSING
                "A" -> SkillCategory.AGILITY
                "G" -> SkillCategory.GENERAL
                "S" -> SkillCategory.STRENGTH
                "M" -> SkillCategory.MUTATIONS
                "T" -> SkillCategory.TRAITS
                else -> throw IllegalStateException("Unsupported skill category: $it")
            }
        }
    }

    private fun convertToBB2020JervisRoster(rules: Rules, roster: FumbbleRosterDetails): BB2020Roster {
        val positions: List<RosterPosition> = roster.positions.map { position ->
            val iconRef = SpriteSheet.fumbbl(position.icon)
            val portraitRef = SingleSprite.fumbbl(position.portrait)
            RosterPosition(
                id = PositionId(position.id),
                quantity = position.quantity,
                title = position.type, // API doesn't return the "group" title, only the singular title
                titleSingular = position.title,
                shortHand = position.iconLetter,
                cost = position.cost,
                move = position.stats.MA,
                strength = position.stats.ST,
                agility = position.stats.AG,
                passing = position.stats.PA,
                armorValue = position.stats.AV,
                skills = position.skills.mapNotNull {
                    convertFumbblSkillToSkillId(rules,it).also {
                        if (it == null) {

                        }
                    }
                },
                primary = mapToSkillCategory(position.normalSkills),
                secondary = mapToSkillCategory(position.doubleSkills),
                icon = iconRef,
                portrait = portraitRef,
            )
        }

        val specialRules = convertRosterSpecialRules(roster.specialRules)

        // All FUMBBL Team logos are rather small, so we use their largest logo
        // for all variants.
        val logo = RosterLogo(
            large = SingleSprite.fumbbl(roster.logos.size192),
            small = SingleSprite.fumbbl(roster.logos.size192),
        )

        return BB2020Roster(
            id = RosterId(roster.id),
            name = roster.name,
            tier = 0, // Unknown
            numberOfRerolls = 8, // Is there a limit?
            rerollCost = roster.rerollCost,
            allowApothecary = (roster.apothecary.equals("yes", ignoreCase = true)),
            specialRules = specialRules,
            positions = positions,
            logo = logo,
        )
    }

    private fun convertRosterSpecialRules(specialRules: List<com.jervisffb.fumbbl.web.api.SpecialRule>): List<SpecialRules> {
        return specialRules.map { fumbblRule ->
            val regionalSpecialRule = RegionalSpecialRule.entries.firstOrNull {
                it.description == fumbblRule.option ?: fumbblRule.name
            }
            val teamSpecialRule = TeamSpecialRule.entries.firstOrNull {
                it.description == fumbblRule.option ?: fumbblRule.name
            }
            regionalSpecialRule ?: teamSpecialRule ?: throw IllegalStateException("Unsupported special rule: $fumbblRule")
        }
    }

    private fun convertTeamSpecialRules(specialRules: com.jervisffb.fumbbl.web.api.SpecialRules): List<SpecialRules> {
        // It is unclear why FUMBBL has a difference in Roster and Team special rules, maybe some customization
        // I am not aware of?
        // For now we just use the ones defined for the Roster (since it has an easier format)
        TODO("Parsing Team special rules is not yet implemented")
    }

    // In FUMBBL, all skills are listed in the same array, so we need to seperate them into 3 buckets:
    // 1) Positional Skills, 2) Extra Skills, 3) Unknown Skills (or other things)
    // Until we know better, we also ignore everything in the 3rd bucket.
    private fun extractExtraSkills(rules: Rules, skills: List<String?>, jervisPosition: Position): List<SkillId> {
        return skills
            .mapNotNull { skillName ->
                skillName?.let { convertFumbblSkillToSkillId(rules, it) }
            }
            .filter { !jervisPosition.skills.contains(it) }
    }

    private fun convertToBB2020JervisTeam(rules: Rules, jervisRoster: BB2020Roster, team: FumbbleTeamDetails): SerializedTeam {
        // Unclear if we want to check for rulesets. Many rulesets will probably have compatible rosters, so it would
        // be unclear how to do that. For now, we will just accept any, and then let it be up to the deserializser
        // to throw an exception if it finds something that isn't supported.
        // if (team.ruleset != 4) throw IllegalStateException("Unsupported ruleset: ${team.ruleset}") // 4 is BB2020

        // Some things are unclear for FUMBBL Teams:
        // - How are player types like MERCENARY and JOURNEYMEN defined?
        // - How are stat increases defined?
        // - Skills (Answer: Looks like the "Skills" array contains both starting + earned skills)
        // - How are injuries defined? Especially niggling and miss next game?
        return SerializedTeam(
            id = TeamId(team.id.toString()),
            name = team.name,
            players = team.players.map { player ->
                val position = jervisRoster.get(PositionId(player.positionId.toString()))
                SerializedPlayer(
                    id = PlayerId(player.id.toString()),
                    name = player.name,
                    number = player.number.playerNo,
                    position = position.id,
                    type = PlayerType.STANDARD, // Unclear if this is always true?
                    statModifiers = emptyList(), // How are these defined?
                    extraSkills = extractExtraSkills(rules,player.skills, position).map { it.toPrettyString() },
                    nigglingInjuries = 0, // Unclear how these are defined
                    missNextGame = false, // Unclear how these are defined
                    starPlayerPoints = player.record.spp,
                    level = PlayerLevel.ROOKIE, // Unclear how this is defined
                    cost = position.cost + player.skillCosts.sum(),
                    icon = PlayerUiData(
                        sprite = position.icon,
                        portrait = position.portrait,
                    )
                )
            },
            roster = jervisRoster,
            rerolls = team.rerolls,
            apothecaries = if (team.apothecary.equals("yes", ignoreCase = true)) 1 else 0,
            cheerleaders = team.cheerleaders,
            assistantCoaches = team.assistantCoaches,
            treasury = team.treasury,
            fanFactor = team.fanFactor,
            teamValue = team.teamValue,
            currentTeamValue = team.currentTeamValue,
            specialRules = jervisRoster.specialRules,
            teamLogo = jervisRoster.logo,
        )
    }

    private fun getBB2020Position(
        jervisRoster: BB2020Roster,
        position: PositionId,
    ): RosterPosition {
        return jervisRoster.positions.firstOrNull {
            it.id  == position
        } ?: error("Unsupported position $position in ${jervisRoster.name}")
    }

    fun close() {
        client.close()
    }
}
