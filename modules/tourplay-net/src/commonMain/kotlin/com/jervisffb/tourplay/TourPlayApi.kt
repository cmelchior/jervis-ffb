package com.jervisffb.tourplay

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
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SerializedPlayer
import com.jervisffb.engine.serialize.SerializedTeam
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.tourplay.api.TourPlayRoster
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Wrapper around the TourPlay REST API: https://tourplay.net
 */
class TourPlayApi() {

    companion object {
        val LOG = jervisLogger()
        val BASE_URL = "https://tourplay.net/api"
    }

    private val client = getHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Load a FUMBBL Team and convert it to a Jervis, so it can be used
     * inside Jervis games.
     */
    suspend fun loadRoster(
        rosterId: Long,
        rules: Rules,
    ): Result<JervisTeamFile> {
        try {
            val rosterData = loadTeamFromTourPlay(rosterId)
            val jervisRoster = convertToBB2020JervisRoster(rules, rosterData)
            val jervisTeam = convertToBB2020JervisTeam(rules, jervisRoster, rosterData)
            return Result.success(JervisTeamFile(
                metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
                team = jervisTeam,
                history = null,
            ))
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    private fun convertToBB2020JervisRoster(rules: Rules, roster: TourPlayRoster): BB2020Roster {
        val positions = roster.rosterMaster.lineUpMasters.map { position ->
            // TODO How to map from TourPlay to FUMBBL Icons / Portraits?
            // As a temporary solution. We need some place holders
            val iconRef = SpriteSheet.generated(position.positionShortHand)
            val portraitRef = SingleSprite.embedded("jervis/portraits/default_portrait.png")
            RosterPosition(
                id = PositionId(position.id.toString()),
                quantity = position.quantity,
                title = position.position, // API doesn't return the "group" title, only the singular title
                titleSingular = position.position,
                shortHand = position.position.first().toString(), // API doesn't have a short hand, just just the first letter (for now)
                cost = position.cost,
                move = position.ma,
                strength = position.st,
                agility = position.ag,
                passing = position.pa,
                armorValue = position.av,
                skills = position.skills.mapNotNull {
                    // TODO It looks like attribute increases are tracked here as well
                    //  Take a closer look at skillAttributeMaster
                    convertTourPlaySkillToSkillId(rules, it.skillMaster.name)
                },
                primary = mapToSkillCategory(position.skillNormal),
                secondary = mapToSkillCategory(position.skillDouble),
                icon = iconRef,
                portrait = portraitRef,
            )
        }
        val specialRules = convertRosterSpecialRules(roster.rosterMaster.teamSpecialRules)

        // TourPlay offer a way to resize the roster logo slightly: `96x<1|2|3>-imageNameInJson`
        // URL: https://tourplay.net/emblems/<rosterId>/96.x1-<rosterId>-5da0zbpz.n5y.png
        // Example: https://tourplay.net/emblems/44442/96.x1-44442-5da0zbpz.n5y.png
        val logo = RosterLogo(
            large = SingleSprite.url("http://tourplay.net/emblems/${roster.id}/96.x3-${roster.imageFile}"),
            small = SingleSprite.url("http://tourplay.net/emblems/${roster.id}/96.x2-${roster.imageFile}"),
        )
        return BB2020Roster(
            id = RosterId(roster.rosterMaster.id.toString()),
            name = roster.rosterMaster.name,
            tier = roster.rosterMaster.tier,
            numberOfRerolls = 8, // Is there a limit?
            rerollCost = roster.rosterMaster.prizeReRoll,
            allowApothecary = roster.rosterMaster.apothecary,
            specialRules = specialRules,
            positions = positions,
            logo = logo,
        )
    }

    /**
     * Map TourPlay Skill Names to Jervis [SkillId]s.
     * Return `null` if the name could not be mapped or if the skill isn't supported
     * by the ruletset.
     */
    fun convertTourPlaySkillToSkillId(rules: Rules, skillName: String): SkillId? {
        // We should probably hard code all the TourPlay titles instead of hoping the names are the same.
        // But for now, we just do it in the few places with known problems and hope for the best.
        val normalizedSkillName = when (skillName) {
            // "Side Step" -> SkillType.SIDESTEP.description
            else -> skillName
        }
        return rules.skillSettings.getSkillIdFromNiceDescription(normalizedSkillName)
    }

    private suspend fun loadTeamFromTourPlay(teamId: Long): TourPlayRoster {
        val response = client.get("$BASE_URL/rosters/$teamId") {
            with(headers) {
                append("Accept", "application/json, text/plain, */*")
                append("Referer", "https://tourplay.net/en/blood-bowl/roster/$teamId")
                append(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
                )
            }
        }
        if (response.status.isSuccess()) {
            val rosterAsJson = response.bodyAsText()
            return json.decodeFromString<TourPlayRoster>(rosterAsJson)
        } else {
            throw IllegalStateException("Loading roster: $teamId failed with status: ${response.status}")
        }
    }

    private fun Int.splitFlags(): List<Int> =
        (0 until Int.SIZE_BITS)
            .map { 1 shl it }
            .filter { this and it != 0 }


    private fun mapToSkillCategory(skillFlags: Int): List<SkillCategory> {
        return skillFlags.splitFlags().map {
            // These values are gathered from manually inspecting the API
            // Agility: 1
            // General: 2
            // Mutation: 4
            // Passing: 8
            // Strength: 16
            when (it) {
                1 -> SkillCategory.AGILITY
                2 -> SkillCategory.GENERAL
                4 -> SkillCategory.MUTATIONS
                8 -> SkillCategory.PASSING
                16 -> SkillCategory.STRENGTH
                else -> error("Unknown skill flag: $it")
            }
        }
    }

    private fun convertRosterSpecialRules(specialRulesFlag: Int): List<SpecialRules> {
        // TourPlay describes their name mapping in the `en.json` file (use Chrome Dev View)
        // This was extracted on 19/08/2025
        //        "1": "Badlands Brawl",
        //        "2": "Elven Kingdoms League",
        //        "4": "Halfling Thimble Cup",
        //        "8": "Lustrian Superleague",
        //        "16": "Old World Classic",
        //        "32": "Sylvanian Spotlight",
        //        "64": "Underworld Challenge",
        //        "128": "Worlds Edge Superleague",
        //        "256": "Bribery and Corruption",
        //        "512": "Favoured of Chaos Undivided",
        //        "1024": "Favoured of Khorne",
        //        "2048": "Favoured of Nurgle",
        //        "4096": "Favoured of Tzeentch",
        //        "8192": "Favoured of Slaanesh",
        //        "16384": "Low Cost Linemen",
        //        "32768": "Master of Undeath",
        //        "65536": "Vampire Lord",
        //        "15872": "Favoured of...",
        //        "146944": "Favoured of...",
        //        "131072": "Favoured of Hashut"
        return specialRulesFlag.splitFlags().mapNotNull { flag ->
            when (flag) {
                // Regional special rules
                1 -> RegionalSpecialRule.BADLANDS_BRAWL
                2 -> RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
                4 -> RegionalSpecialRule.HAFLING_THIMBLE_CUP // Note: enum uses HAFLING (as defined)
                8 -> RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE
                16 -> RegionalSpecialRule.OLD_WORLD_CLASSIC
                32 -> RegionalSpecialRule.SYLVANIAN_SPOTLIGHT
                64 -> RegionalSpecialRule.UNDERWORLD_CHALLENGE
                128 -> RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE

                // Team special rules
                256 -> TeamSpecialRule.BRIBERY_AND_CORRUPTION
                512 -> TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
                1024 -> TeamSpecialRule.FAVOURED_OF_KHORNE
                2048 -> TeamSpecialRule.FAVOURED_OF_NURGLE
                4096 -> TeamSpecialRule.FAVOURED_OF_TZEENTCH
                8192 -> TeamSpecialRule.FAVOURED_OF_SLAANESH
                16384 -> TeamSpecialRule.LOW_COST_LINEMEN
                32768 -> TeamSpecialRule.MASTERS_OF_UNDEATH
                //        "65536": "Vampire Lord",
                //        "15872": "Favoured of...",
                131072 -> TeamSpecialRule.FAVOURED_OF_HASHUT
                //        "146944": "Favoured of...",
                else -> {
                    LOG.d { "Could not map special rule flag: $flag" }
                    null
                }
            }
        }
    }

    private fun convertToBB2020JervisTeam(rules: Rules, jervisRoster: BB2020Roster, team: TourPlayRoster): SerializedTeam {
        // This only supports basic conversion.
        // Some things are unclear for FUMBBL Teams:
        // - How are player types like MERCENARY and JOURNEYMEN defined?
        // - How are stat increases defined?
        // - How are injuries defined? Especially niggling and miss next game?
        return SerializedTeam(
            id = TeamId(team.id.toString()),
            name = team.teamName,
            // Right now we just assume that the FUMBBL team matches the given game type
            // We probably need to refine this later.
            type = rules.gameType,
            players = team.lineUps.map { player ->
                val position = jervisRoster.positions.first { it.id.value == player.lineUpMaster.id.toString() }
                SerializedPlayer(
                    id = PlayerId(player.id.toString()),
                    name = player.name,
                    number = player.number.playerNo,
                    position = position.id,
                    type = PlayerType.STANDARD, // Unclear if this is always true?
                    statModifiers = emptyList(), // How are these defined?
                    extraSkills = player.skills.mapNotNull {
                        convertTourPlaySkillToSkillId(rules, it.skillMaster.name)
                    }.map { it.serialize() },
                    nigglingInjuries = 0, // Unclear how these are defined
                    missNextGame = false, // Unclear how these are defined
                    starPlayerPoints = player.starPlayerPoints,
                    level = PlayerLevel.ROOKIE, // Unclear how this is defined
                    cost = position.cost + player.cost,
                    icon = PlayerUiData(
                        sprite = position.icon,
                        portrait = position.portrait,
                    )
                )
            },
            roster = jervisRoster,
            rerolls = team.reRolls,
            apothecaries = if (team.apothecary) 1 else 0,
            cheerleaders = team.cheerLeaders,
            assistantCoaches = team.assistantCoaches,
            treasury = team.treasury,
            fanFactor = team.fanFactor,
            teamValue = team.teamValue * 1000, // TourPlay tracks Team Value as its "short hand" value
            currentTeamValue = team.teamValue * 1000, // Unclear if this is current or something else?
            specialRules = jervisRoster.specialRules,
            teamLogo = jervisRoster.logo,
        )
    }
}
