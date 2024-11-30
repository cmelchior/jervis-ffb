package com.jervisffb.fumbbl.web

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.common.roster.PositionId
import com.jervisffb.engine.rules.common.roster.RosterId
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.RosterSpriteData
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.engine.serialize.TeamSpriteData
import com.jervisffb.engine.teamBuilder
import com.jervisffb.fumbbl.web.api.PlayerDetails
import com.jervisffb.fumbbl.web.api.RosterDetails
import com.jervisffb.fumbbl.web.api.TeamDetails
import com.jervisffb.utils.getHttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class FumbblTeamLoader {

    val client = getHttpClient()
    val json = Json { ignoreUnknownKeys = true }

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
                roster = jervisRoster,
                team = jervisTeam,
                history = null,
                rosterUiData = createRosterSpriteData(roster),
                uiData = createTeamSpriteData(team),
            )
    }

    private suspend fun loadRosterFromFumbbl(rosterId: Int): RosterDetails {
        val result = client.get("https://fumbbl.com/api/roster/get/$rosterId")
        if (result.status.isSuccess()) {
            return json.decodeFromString<com.jervisffb.fumbbl.web.api.RosterDetails>(result.bodyAsText())
        } else {
            throw IllegalStateException("Loading roster $rosterId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamFromFumbbl(teamId: Int): TeamDetails {
        val result = client.get("https://fumbbl.com/api/team/get/$teamId")
        if (result.status.isSuccess()) {
            return json.decodeFromString<TeamDetails>(result.bodyAsText())
        } else {
            throw IllegalStateException("Loading team $teamId failed with status ${result.status}")
        }
    }

    private suspend fun loadTeamPlayers(team: TeamDetails): Set<PlayerDetails> {
        return team.players.map { player -> loadPlayer(player.id) }.toSet()
    }

    private suspend fun loadPlayer(playerId: Int): PlayerDetails {
        val result = client.get("https://fumbbl.com/api/player/get/$playerId")
        val details = json.decodeFromString<PlayerDetails>(result.bodyAsText())
        return details
    }

    private suspend fun createTeamSpriteData(team: TeamDetails): TeamSpriteData {
        val playerData = loadTeamPlayers(team)
        return TeamSpriteData(
            teamLogo = null, // How to get this?
            players = playerData.associate {
                Pair(
                    PlayerId(it.id.toString()), PlayerUiData(
                        sprite = SpriteSheet.fumbbl(it.icon),
                        portrait = SingleSprite.fumbbl(it.portrait)
                    )
                )
            }
        )
    }

    private fun createRosterSpriteData(roster: RosterDetails): RosterSpriteData {
        return RosterSpriteData(
            rosterLogo = SingleSprite.fumbbl(roster.logos.value),
            positions = roster.positions.associate {
                Pair(
                    PositionId(it.id),
                    SingleSprite.fumbbl(it.icon)
                )
            },
            portraits = roster.positions.associate {
                Pair(
                    PositionId(it.id),
                    SingleSprite.fumbbl(it.portrait)
                )
            }
        )
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
                position = position.type, // API doesn't return the "group" title, only the singular title
                positionSingular = position.type,
                shortHand = position.iconLetter,
                cost = position.cost,
                move = position.stats.MA,
                strenght = position.stats.ST,
                agility = position.stats.AG,
                passing = position.stats.PA,
                armorValue = position.stats.AV,
                skills = mapToSkillFactory(position.skills),
                primary = mapToSkillCategory(position.normalSkills),
                secondary = mapToSkillCategory(position.doubleSkills)
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
            positions = positions
        )
    }

    // Convert a FUMBBL Team Data into a Jervis Team
    private fun convertToBB2020JervisTeam(jervisRoster: BB2020Roster, team: TeamDetails): Team {
        if (team.ruleset != 4) throw IllegalStateException("Unsupported ruleset ${team.ruleset}") // 4 is BB2020
        return teamBuilder(StandardBB2020Rules, jervisRoster) {
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
