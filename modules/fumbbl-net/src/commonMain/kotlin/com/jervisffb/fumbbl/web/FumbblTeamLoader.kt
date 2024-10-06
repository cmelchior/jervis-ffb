package com.jervisffb.fumbbl.web

import com.jervisffb.fumbbl.net.api.auth.getHttpClient
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.HumanTeam
import com.jervisffb.engine.teamBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.core.use
import kotlinx.serialization.json.Json

object FumbblTeamLoader {
    val json = Json { ignoreUnknownKeys = true }

    /**
     * Load a FUMBBL Team and convert it to a Jervis, so it can be used
     * inside Jervis games.
     */
    suspend fun loadTeam(
        teamId: Int,
        rules: Rules,
    ): Team {
        return getHttpClient().use { client ->
            val result = client.get("https://fumbbl.com/api/team/get/$teamId")
            if (result.status.isSuccess()) {
                val fumbblTeam = com.jervisffb.fumbbl.web.FumbblTeamLoader.json.decodeFromString<com.jervisffb.fumbbl.web.api.Team>(result.bodyAsText())
                com.jervisffb.fumbbl.web.FumbblTeamLoader.convertToBB2020JervisTeam(fumbblTeam)
            } else {
                throw IllegalStateException("Loading team $teamId failed with status ${result.status}")
            }
        }
    }

    // Convert a FUMBBL Team Data into a Jervis Team
    private fun convertToBB2020JervisTeam(team: com.jervisffb.fumbbl.web.api.Team): Team {
        if (team.ruleset != 4) throw IllegalStateException("Unsupported ruleset ${team.ruleset}") // 4 is BB2020
        val jervisRoster: BB2020Roster = com.jervisffb.fumbbl.web.FumbblTeamLoader.getBB2020Roster(team.roster)
        return teamBuilder(StandardBB2020Rules, jervisRoster) {
            name = team.name
            coach = Coach(CoachId(team.coach.id.toString()), team.coach.name)
            reRolls = team.rerolls
            fanFactor = team.fanFactor
            cheerLeaders = team.cheerleaders
            assistentCoaches = team.assistantCoaches
            // apothecaries = team.apothecary
            team.players.forEach { player ->
                val id = PlayerId(player.id.toString())
                val name = player.name
                val number = PlayerNo(player.number)
                val position =
                    getBB2020Position(jervisRoster, player.position.name)
                addPlayer(id, name, number, position)
            }
        }
    }

    private fun getBB2020Position(
        jervisRoster: BB2020Roster,
        name: String,
    ): BB2020Position {
        // This might not be true for all FUMBBL names. Some will probably crash. A problem for the future
        return jervisRoster.positions.firstOrNull {
            it.positionSingular == name
        } ?: error("Unsupported position $name in ${jervisRoster.name}")
    }

    private fun getBB2020Roster(roster: com.jervisffb.fumbbl.web.api.Roster): BB2020Roster {
        return when (roster.name) {
            "Human" -> HumanTeam
            else -> error("Unsupported roster ${roster.name}")
        }
    }
}
