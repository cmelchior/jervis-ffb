package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.commands.ResetAvailableTeamRerolls
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.teamBuilder


fun humanTeamAway(): Team {
    return teamBuilder(StandardBB2020Rules(), HUMAN_TEAM) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Lineman-1-A", PlayerNo(1), HUMAN_LINEMAN)
        addPlayer(PlayerId("A2"), "Lineman-2-A", PlayerNo(2), HUMAN_LINEMAN)
        addPlayer(PlayerId("A3"), "Lineman-3-A", PlayerNo(3), HUMAN_LINEMAN)
        addPlayer(PlayerId("A4"), "Lineman-4-A", PlayerNo(4), HUMAN_LINEMAN)
        addPlayer(PlayerId("A5"), "Thrower-5-A", PlayerNo(5), HUMAN_LINEMAN)
        addPlayer(PlayerId("A6"), "Catcher-6-A", PlayerNo(6), HUMAN_LINEMAN)
        addPlayer(PlayerId("A7"), "Catcher-7-A", PlayerNo(7), HUMAN_CATCHER)
        addPlayer(PlayerId("A8"), "Blitzer-8-A", PlayerNo(8), HUMAN_BLITZER)
        addPlayer(PlayerId("A9"), "Blitzer-9-A", PlayerNo(9), HUMAN_BLITZER)
        addPlayer(PlayerId("A10"), "Blitzer-10-A", PlayerNo(10), HUMAN_BLITZER)
        addPlayer(PlayerId("A11"), "Blitzer-11-A", PlayerNo(11), HUMAN_BLITZER)
        addPlayer(PlayerId("A12"), "Lineman-12-A", PlayerNo(12), HUMAN_LINEMAN)
        reRolls = 4
        apothecaries = 1
        dedicatedFans = 2
        teamValue = 1_000_000
    }
}

fun lizardMenAwayTeam(): Team {
    return teamBuilder(StandardBB2020Rules(), LIZARDMEN_TEAM) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Kroxigor-1-A", PlayerNo(1), KROXIGOR)
        addPlayer(PlayerId("A2"), "Saurus-2-A", PlayerNo(2), SAURUS_BLOCKERS)
        addPlayer(PlayerId("A3"), "Saurus-3-A", PlayerNo(3), SAURUS_BLOCKERS)
        addPlayer(PlayerId("A4"), "Saurus-4-A", PlayerNo(4), SAURUS_BLOCKERS)
        addPlayer(PlayerId("A5"), "Saurus-5-A", PlayerNo(5), SAURUS_BLOCKERS)
        addPlayer(PlayerId("A6"), "Saurus-6-A", PlayerNo(6), SAURUS_BLOCKERS, listOf(Frenzy()))
        addPlayer(PlayerId("A7"), "Saurus-7-A", PlayerNo(7), SAURUS_BLOCKERS, listOf(Frenzy()))
        addPlayer(PlayerId("A8"), "ChameleonSkink-8-A", PlayerNo(8), CHAMELEON_SKINKS)
        addPlayer(PlayerId("A9"), "Skink-9-A", PlayerNo(9), SKINK_RUNNER_LINEMEN)
        addPlayer(PlayerId("A10"), "Skink-10-A", PlayerNo(10), SKINK_RUNNER_LINEMEN)
        addPlayer(PlayerId("A11"), "Skink-11-A", PlayerNo(11), SKINK_RUNNER_LINEMEN)
        reRolls = 4
        apothecaries = 1
        teamValue = 1_000_000
    }
}

/**
 * Default setup of two test teams.
 *
 * They will be set up in a mirror way.
 *
 * - 1-5 Are setup in the midle of the LoS
 * - 6-7 are setup next to each other on the right line 1 step away from LoS
 * - 8-9 are setup next to each on the left line 1 step away from LoS
 * - 10-11 are setup in the backfield
 */
fun setupTeamsOnField(controller: GameEngineController) {
    val homeCommands = with(controller.state.homeTeam) {
        listOf(
            SetPlayerLocation(get(PlayerNo(1)), FieldCoordinate(12, 5)),
            SetPlayerLocation(get(PlayerNo(2)), FieldCoordinate(12, 6)),
            SetPlayerLocation(get(PlayerNo(3)), FieldCoordinate(12, 7)),
            SetPlayerLocation(get(PlayerNo(4)), FieldCoordinate(12, 8)),
            SetPlayerLocation(get(PlayerNo(5)), FieldCoordinate(12, 9)),
            SetPlayerLocation(get(PlayerNo(6)), FieldCoordinate(11, 1)),
            SetPlayerLocation(get(PlayerNo(7)), FieldCoordinate(11, 2)),
            SetPlayerLocation(get(PlayerNo(8)), FieldCoordinate(11, 12)),
            SetPlayerLocation(get(PlayerNo(9)), FieldCoordinate(11, 13)),
            SetPlayerLocation(get(PlayerNo(10)), FieldCoordinate(9, 7)),
            SetPlayerLocation(get(PlayerNo(11)), FieldCoordinate(3, 7))
        )
    }
    val awayCommands = with(controller.state.awayTeam) {
        listOf(
            SetPlayerLocation(get(PlayerNo(1)), FieldCoordinate(13, 5)),
            SetPlayerLocation(get(PlayerNo(2)), FieldCoordinate(13, 6)),
            SetPlayerLocation(get(PlayerNo(3)), FieldCoordinate(13, 7)),
            SetPlayerLocation(get(PlayerNo(4)), FieldCoordinate(13, 8)),
            SetPlayerLocation(get(PlayerNo(5)), FieldCoordinate(13, 9)),
            SetPlayerLocation(get(PlayerNo(6)), FieldCoordinate(14, 1)),
            SetPlayerLocation(get(PlayerNo(7)), FieldCoordinate(14, 2)),
            SetPlayerLocation(get(PlayerNo(8)), FieldCoordinate(14, 12)),
            SetPlayerLocation(get(PlayerNo(9)), FieldCoordinate(14, 13)),
            SetPlayerLocation(get(PlayerNo(10)), FieldCoordinate(16, 7)),
            SetPlayerLocation(get(PlayerNo(11)), FieldCoordinate(22, 7))
        )
    }

    (homeCommands + awayCommands).forEach { command ->
        command.execute(controller.state)
    }

    // Also enable Team rerolls
    controller.state.activeTeam = controller.state.homeTeam
    ResetAvailableTeamRerolls(controller.state.homeTeam).execute(controller.state)
    ResetAvailableTeamRerolls(controller.state.awayTeam).execute(controller.state)
}

fun createDefaultHomeTeam(): Team {
    return teamBuilder(StandardBB2020Rules(), HUMAN_TEAM) {
        coach = Coach(CoachId("home-coach"), "HomeCoach")
        name = "HomeTeam"
        addPlayer(PlayerId("H1"), "Lineman-1-H", PlayerNo(1), HUMAN_LINEMAN)
        addPlayer(PlayerId("H2"), "Lineman-2-H", PlayerNo(2), HUMAN_LINEMAN)
        addPlayer(PlayerId("H3"), "Lineman-3-H", PlayerNo(3), HUMAN_LINEMAN)
        addPlayer(PlayerId("H4"), "Lineman-4-H", PlayerNo(4), HUMAN_LINEMAN)
        addPlayer(PlayerId("H5"), "Thrower-5-H", PlayerNo(5), HUMAN_THROWER, listOf(SideStep()))
        addPlayer(PlayerId("H6"), "Catcher-6-H", PlayerNo(6), HUMAN_CATCHER, listOf(SideStep()))
        addPlayer(PlayerId("H7"), "Catcher-7-H", PlayerNo(7), HUMAN_CATCHER)
        addPlayer(PlayerId("H8"), "Blitzer-8-H", PlayerNo(8), HUMAN_BLITZER)
        addPlayer(PlayerId("H9"), "Blitzer-9-H", PlayerNo(9), HUMAN_BLITZER)
        addPlayer(PlayerId("H10"), "Blitzer-10-H", PlayerNo(10), HUMAN_BLITZER)
        addPlayer(PlayerId("H11"), "Blitzer-11-H", PlayerNo(11), HUMAN_BLITZER)
        addPlayer(PlayerId("H12"), "Lineman-12-H", PlayerNo(12), HUMAN_LINEMAN)
        reRolls = 4
        apothecaries = 1
        dedicatedFans = 1
        teamValue = 1_000_000
    }
}

fun createDefaultGameState(rules: Rules, awayTeam: Team = humanTeamAway()): Game {
    val field = Field.createForRuleset(rules)
    return Game(rules, createDefaultHomeTeam(), awayTeam, field)
}

/**
 * Move all players onto the field as if starting a game.
 * Only works on the setup defined above
 */
fun createStartingTestSetup(state: Game) {
    fun setupPlayer(
        state: Game,
        player: Player?,
        fieldCoordinate: FieldCoordinate,
    ) {
        player?.let {
            SetPlayerLocation(it, fieldCoordinate).execute(state)
            SetPlayerState(it, PlayerState.STANDING)
        } ?: error("")
    }

    // Home
    with(state.homeTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(12, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(12, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(12, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(10, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(10, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(10, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(10, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(8, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(8, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(8, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(8, 13))
    }

    // Away
    with(state.awayTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(13, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(13, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(13, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(15, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(15, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(15, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(15, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(17, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(17, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(17, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(17, 13))
    }
}
