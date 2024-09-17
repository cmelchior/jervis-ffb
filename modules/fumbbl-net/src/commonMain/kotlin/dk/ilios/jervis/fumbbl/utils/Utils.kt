package dk.ilios.jervis.fumbbl.utils

import dk.ilios.jervis.fumbbl.model.SpecialRule
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.BB2020Roster
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam
import dk.ilios.jervis.rules.roster.bb2020.ElvenUnionTeam
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.KhorneTeam
import dk.ilios.jervis.rules.roster.bb2020.RegionalSpecialRule
import dk.ilios.jervis.rules.roster.bb2020.SkavenTeam
import dk.ilios.jervis.rules.roster.bb2020.TeamSpecialRule
import dk.ilios.jervis.rules.skills.Block
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.Tackle
import dk.ilios.jervis.rules.skills.Wrestle
import dk.ilios.jervis.teamBuilder

typealias FumbblGame = dk.ilios.jervis.fumbbl.model.Game
typealias FumbblTeam = dk.ilios.jervis.fumbbl.model.Team
typealias FumbblField = dk.ilios.jervis.fumbbl.model.FieldModel
typealias FumbblRoster = dk.ilios.jervis.fumbbl.model.Roster
typealias FumbblPlayer = dk.ilios.jervis.fumbbl.model.Player
typealias FumbblCoordinate = dk.ilios.jervis.fumbbl.model.FieldCoordinate

/**
 * Convert a FUMBBL Game Model into the equivalent Jervis Game Model.
 *
 * This can be used to bootstrap the game model from FUMBBL Replay files.
 *
 * @see [dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay].
 */
fun Game.Companion.fromFumbblState(rules: Rules, game: FumbblGame): Game {
    val homeTeam = extractTeam(rules, game.teamHome)
    val awayTeam = extractTeam(rules, game.teamAway)
    val field: Field = extractField(game.fieldModel)
    return Game(homeTeam, awayTeam, field)
}

private fun extractTeam(rules: Rules, team: FumbblTeam): dk.ilios.jervis.model.Team {
    val roster = extractRoster(team.roster)
    return teamBuilder(rules, roster) {
        this.name = team.teamName
        this.coach = Coach(id = CoachId(team.coach), name = team.coach)
        // val race Something we care about?
        // val baseIconPath: String, // This is relevant for the UI model. Figure out how to include this
        // val logoUrl: String?, // This is relevant for the UI model. Figure out how to include this
        this.reRolls = team.reRolls
        this.apothecaries = team.apothecaries
        this.cheerLeaders = team.cheerleaders
        this.assistentCoaches = team.assistantCoaches
        this.fanFactor = team.fanFactor
        this.teamValue = team.teamValue
        this.dedicatedFans = team.dedicatedFans
        team.specialRules.forEach {
            val specialRule =
                when (it) {
                    SpecialRule.BADLANDS_BRAWL -> RegionalSpecialRule.BADLANDS_BRAWL
                    SpecialRule.BRIBERY_AND_CORRUPTION -> TeamSpecialRule.BRIBERY_AND_CORRUPTION
                    SpecialRule.ELVEN_KINGDOMS_LEAGUE -> RegionalSpecialRule.ELVEN_KINGDOM_LEAGUE
                    SpecialRule.FAVOURED_OF_KHORNE -> TeamSpecialRule.FAVOURED_OF_KHORNE
                    SpecialRule.FAVOURED_OF_NURGLE -> TeamSpecialRule.FAVOURED_OF_NURGLE
                    SpecialRule.FAVOURED_OF_SLAANESH -> TeamSpecialRule.FAVOURED_OF_SLAANESH
                    SpecialRule.FAVOURED_OF_TZEENTCH -> TeamSpecialRule.FAVOURED_OF_TZEENTCH
                    SpecialRule.FAVOURED_OF_UNDIVIDED -> TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
                    SpecialRule.HALFLING_THIMBLE_CUP -> RegionalSpecialRule.HAFLING_THIMBLE_CUP
                    SpecialRule.LOW_COST_LINEMEN -> TeamSpecialRule.LOW_COST_LINEMEN
                    SpecialRule.LUSTRIAN_SUPERLEAGUE -> RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE
                    SpecialRule.MASTERS_OF_UNDEATH -> TeamSpecialRule.MASTERS_OF_UNDEATH
                    SpecialRule.OLD_WORLD_CLASSIC -> RegionalSpecialRule.OLD_WORLD_CLASSIC
                    SpecialRule.SYLVANIAN_SPOTLIGHT -> RegionalSpecialRule.SYLVIAN_SPOTLIGHT
                    SpecialRule.UNDERWORLD_CHALLENGE -> RegionalSpecialRule.UNDERWORLD_CHALLENGE
                    SpecialRule.WORLDS_EDGE_SUPERLEAGUE -> RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE
                }
            this.specialRules.add(specialRule)
        }
        team.players.forEach { fumbblPlayer: FumbblPlayer ->
            val fumbblPosition = team.roster.positions.firstOrNull { it.positionId == fumbblPlayer.positionId }
            if (fumbblPosition == null) {
                throw IllegalStateException("Could not find matching position: ${fumbblPlayer.positionId}")
            }
            val position = roster.positions.firstOrNull { it.positionSingular == fumbblPosition.positionName }
            if (position == null) {
                throw IllegalStateException(
                    "Could not find position '${fumbblPosition.positionName}' in '${team.roster.rosterName}'",
                )
            }
            val skills = fumbblPlayer.skillValuesMap.map {
                when (it.key) {
                    "Block" -> Block()
                    "Dodge" -> Dodge()
                    "Tackle" -> Tackle()
                    "Wrestle" -> Wrestle()
                    "Extra Arms",
                    "Brawler",
                    "Sneaky Git",
                    "Leader" -> null // TODO
                    else -> TODO("Unsupported skill: $it")
                }
            }.filterNotNull()

            addPlayer(
                PlayerId(fumbblPlayer.playerId),
                fumbblPlayer.playerName,
                PlayerNo(fumbblPlayer.playerNr),
                position,
                skills
            )
        }
    }
}

private fun extractRoster(roster: FumbblRoster): BB2020Roster {
    // TODO Add logic for building custom rosters, for now
    //  just refer to the original rules
    return when (roster.rosterName) {
        "Chaos Dwarf" -> ChaosDwarfTeam
        "Human" -> HumanTeam
        "Khorne" -> KhorneTeam
        "Elven Union" -> ElvenUnionTeam
        "Skaven" -> SkavenTeam
        else -> TODO("Missing team: ${roster.rosterName}")
    }
}

private fun extractField(field: FumbblField): Field {
    // TODO Extract more information when we know what to fetch
    return Field(width = 26, height = 15)
}
