package dk.ilios.jervis.fumbbl.utils

import dk.ilios.jervis.fumbbl.model.SpecialRule
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.BadlandsBrawl
import dk.ilios.jervis.rules.roster.bb2020.BriberyAndCorruption
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam
import dk.ilios.jervis.rules.roster.bb2020.ElvenKingdomLeague
import dk.ilios.jervis.rules.roster.bb2020.ElvenUnionTeam
import dk.ilios.jervis.rules.roster.bb2020.FavouredOfChaosUndivided
import dk.ilios.jervis.rules.roster.bb2020.FavouredOfKhorne
import dk.ilios.jervis.rules.roster.bb2020.FavouredOfNurgle
import dk.ilios.jervis.rules.roster.bb2020.FavouredOfSlaanesh
import dk.ilios.jervis.rules.roster.bb2020.FavouredOfTzeentch
import dk.ilios.jervis.rules.roster.bb2020.HalflingThimbleCup
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.KhorneTeam
import dk.ilios.jervis.rules.roster.bb2020.LowCostLinemen
import dk.ilios.jervis.rules.roster.bb2020.LustrianSuperLeague
import dk.ilios.jervis.rules.roster.bb2020.MastersOfUndeath
import dk.ilios.jervis.rules.roster.bb2020.OldWorldClassic
import dk.ilios.jervis.rules.roster.bb2020.SkavenTeam
import dk.ilios.jervis.rules.roster.bb2020.SylvanianSpotlight
import dk.ilios.jervis.rules.roster.bb2020.UnderworldChallenge
import dk.ilios.jervis.rules.roster.bb2020.WorldsEdgeSuperLeague
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
fun Game.Companion.fromFumbblState(game: FumbblGame): Game {
    val homeTeam = extractTeam(game.teamHome)
    val awayTeam = extractTeam(game.teamAway)
    val field: Field = extractField(game.fieldModel)
    return Game(homeTeam, awayTeam, field)
}

private fun extractTeam(team: FumbblTeam): dk.ilios.jervis.model.Team {
    val roster = extractRoster(team.roster)
    return teamBuilder(roster) {
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
                    SpecialRule.BADLANDS_BRAWL -> BadlandsBrawl
                    SpecialRule.BRIBERY_AND_CORRUPTION -> BriberyAndCorruption
                    SpecialRule.ELVEN_KINGDOMS_LEAGUE -> ElvenKingdomLeague
                    SpecialRule.FAVOURED_OF_KHORNE -> FavouredOfKhorne
                    SpecialRule.FAVOURED_OF_NURGLE -> FavouredOfNurgle
                    SpecialRule.FAVOURED_OF_SLAANESH -> FavouredOfSlaanesh
                    SpecialRule.FAVOURED_OF_TZEENTCH -> FavouredOfTzeentch
                    SpecialRule.FAVOURED_OF_UNDIVIDED -> FavouredOfChaosUndivided
                    SpecialRule.HALFLING_THIMBLE_CUP -> HalflingThimbleCup
                    SpecialRule.LOW_COST_LINEMEN -> LowCostLinemen
                    SpecialRule.LUSTRIAN_SUPERLEAGUE -> LustrianSuperLeague
                    SpecialRule.MASTERS_OF_UNDEATH -> MastersOfUndeath
                    SpecialRule.OLD_WORLD_CLASSIC -> OldWorldClassic
                    SpecialRule.SYLVANIAN_SPOTLIGHT -> SylvanianSpotlight
                    SpecialRule.UNDERWORLD_CHALLENGE -> UnderworldChallenge
                    SpecialRule.WORLDS_EDGE_SUPERLEAGUE -> WorldsEdgeSuperLeague
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
            addPlayer(
                PlayerId(fumbblPlayer.playerId),
                fumbblPlayer.playerName,
                PlayerNo(fumbblPlayer.playerNr),
                position,
            )
        }
    }
}

private fun extractRoster(roster: FumbblRoster): Roster {
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
