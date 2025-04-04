package com.jervisffb.fumbbl.web

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.teamBuilder
import com.jervisffb.fumbbl.web.api.AuthResult
import com.jervisffb.fumbbl.web.api.CoachSearchResult
import com.jervisffb.fumbbl.web.api.CurrentMatchResult
import com.jervisffb.fumbbl.web.api.PlayerDetails
import com.jervisffb.fumbbl.web.api.RosterDetails
import com.jervisffb.fumbbl.web.api.TeamDetails
import com.jervisffb.utils.getHttpClient
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
            authToken = json.decodeFromString(response.bodyAsText())
            oauthToken = authToken?.accessToken
            coachId = getCoachId(coachName)
            if (coachId == null) {
                authToken = null
                oauthToken = null
                coachId = null
                throw IllegalStateException("Unable to find coach id for $coachName")
            }
            authToken!!
        } else {
            throw IllegalStateException("Authentication failed with status ${response.status}")
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
        teamId: Int,
        rules: Rules,
    ): JervisTeamFile {
        val team = loadTeamFromFumbbl(teamId)
        val roster = loadRosterFromFumbbl(team.roster.id)
        val players: Set<PlayerDetails> = loadTeamPlayers(team)
        val jervisRoster = convertToBB2020JervisRoster(roster)
        val jervisTeam = convertToBB2020JervisTeam(jervisRoster, team)
        return JervisTeamFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            team = jervisTeam,
            history = null,
        )
    }

    private suspend fun loadRosterFromFumbbl(rosterId: Int): RosterDetails {
        val result = client.get("$BASE_URL/roster/get/$rosterId")
        if (result.status.isSuccess()) {
            return json.decodeFromString<com.jervisffb.fumbbl.web.api.RosterDetails>(result.bodyAsText())
        } else {
            throw IllegalStateException("Loading roster $rosterId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamFromFumbbl(teamId: Int): TeamDetails {
        val result = client.get("$BASE_URL/team/get/$teamId")
        if (result.status.isSuccess()) {
            return result.body<TeamDetails>()
        } else {
            throw IllegalStateException("Loading team $teamId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamPlayers(team: TeamDetails): Set<PlayerDetails> {
        return team.players.map { player -> loadPlayer(player.id) }.toSet()
    }

    private suspend fun loadPlayer(playerId: Int): PlayerDetails {
        val result = client.get("$BASE_URL/player/get/$playerId")
        val details = json.decodeFromString<PlayerDetails>(result.bodyAsText())
        return details
    }

    private fun mapToSkillFactory(skills: List<String>): List<SkillFactory> {
        // We should probably hard code all the FUMBBL titles instead of hoping the nams are the same.
        // Also, this is allocating way too many objects.
        return skills.mapNotNull { fumbblSkill ->
            BB2020SkillCategory.entries.flatMap { it.skills }
                .firstOrNull {
                    it.createSkill().name == fumbblSkill
                } // ?: throw IllegalStateException("Unsupported skill $fumbblSkill")
        }
    }

    private fun mapToSkillCategory(categories: List<String>): List<BB2020SkillCategory> {
        return categories.map {
            when (it) {
                "P" -> BB2020SkillCategory.PASSING
                "A" -> BB2020SkillCategory.AGILITY
                "G" -> BB2020SkillCategory.GENERAL
                "S" -> BB2020SkillCategory.STRENGTH
                "M" -> BB2020SkillCategory.MUTATIONS
                "T" -> BB2020SkillCategory.TRAITS
                else -> throw IllegalStateException("Unsupported skill category: $it")
            }
        }
    }

    private fun convertToBB2020JervisRoster(roster: RosterDetails): BB2020Roster {
        val positions: List<BB2020Position> = roster.positions.map { position ->
            BB2020Position(
                id = PositionId(position.id),
                quantity = position.quantity,
                title = position.type, // API doesn't return the "group" title, only the singular title
                titleSingular = position.type,
                shortHand = position.iconLetter,
                cost = position.cost,
                move = position.stats.MA,
                strength = position.stats.ST,
                agility = position.stats.AG,
                passing = position.stats.PA,
                armorValue = position.stats.AV,
                skills = mapToSkillFactory(position.skills),
                primary = mapToSkillCategory(position.normalSkills),
                secondary = mapToSkillCategory(position.doubleSkills),
                icon = null,
                portrait = null,
            )
        }

        val specialRules = roster.specialRules.map { fumbblRule ->
            val regionalSpecialRule = RegionalSpecialRule.entries.firstOrNull {
                it.description == fumbblRule.name
            }
            val teamSpecialRule = TeamSpecialRule.entries.firstOrNull {
                it.description == fumbblRule.name
            }
            regionalSpecialRule ?: teamSpecialRule ?: throw IllegalStateException("Unsupported special rule $fumbblRule")
        }

        return BB2020Roster(
            id = RosterId(roster.id),
            name = roster.name,
            tier = 0, // Unknown
            numberOfRerolls = 8, // Is there a limit?
            rerollCost = roster.rerollCost,
            allowApothecary = (roster.apothecary.equals("yes", ignoreCase = true)),
            specialRules = specialRules,
            positions = positions,
            logo = RosterLogo.NONE,
        )
    }

    // Convert a FUMBBL Team Data into a Jervis Team
    private fun convertToBB2020JervisTeam(jervisRoster: BB2020Roster, team: TeamDetails): Team {
        if (team.ruleset != 4) throw IllegalStateException("Unsupported ruleset ${team.ruleset}") // 4 is BB2020
        return teamBuilder(StandardBB2020Rules(), jervisRoster) {
            id = TeamId(team.id.toString())
            name = team.name
            teamValue = team.teamValue
            coach = Coach(CoachId(team.coach.id.toString()), team.coach.name)
            reRolls = team.rerolls
            fanFactor = team.fanFactor
            cheerLeaders = team.cheerleaders
            assistentCoaches = team.assistantCoaches
            apothecaries = if (team.apothecary.equals("yes", ignoreCase = true)) 1 else 0
            team.players.forEach { player ->
                val id = PlayerId(player.id.toString())
                val name = player.name
                val number = PlayerNo(player.number)
                val position = getBB2020Position(jervisRoster, PositionId(player.positionId.toString()))
                addPlayer(id, name, number, position)
            }
        }
    }

    private fun getBB2020Position(
        jervisRoster: BB2020Roster,
        position: PositionId,
    ): BB2020Position {
        return jervisRoster.positions.firstOrNull {
            it.id  == position
        } ?: error("Unsupported position $position in ${jervisRoster.name}")
    }

    fun close() {
        client.close()
    }
}
