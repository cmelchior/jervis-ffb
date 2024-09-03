package com.jervis.fumbbl

import com.jervis.fumbbl.restapi.Roster
import dk.ilios.jervis.fumbbl.net.auth.getHttpClient
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.BB2020Position
import dk.ilios.jervis.rules.roster.bb2020.BB2020Roster
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.teamBuilder
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
                val fumbblTeam = json.decodeFromString<com.jervis.fumbbl.restapi.Team>(result.bodyAsText())
                convertToBB2020JervisTeam(fumbblTeam)
            } else {
                throw IllegalStateException("Loading team $teamId failed with status ${result.status}")
            }
        }
    }

    // Convert a FUMBBL Team Data into a Jervis Team
    private fun convertToBB2020JervisTeam(team: com.jervis.fumbbl.restapi.Team): Team {
        if (team.ruleset != 4) throw IllegalStateException("Unsupported ruleset ${team.ruleset}") // 4 is BB2020
        val jervisRoster: BB2020Roster = getBB2020Roster(team.roster)
        return teamBuilder(BB2020Rules, jervisRoster) {
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
                val position = getBB2020Position(jervisRoster, player.position.name)
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

    private fun getBB2020Roster(roster: Roster): BB2020Roster {
        return when (roster.name) {
            "Human" -> HumanTeam
            else -> error("Unsupported roster ${roster.name}")
        }
    }
}
