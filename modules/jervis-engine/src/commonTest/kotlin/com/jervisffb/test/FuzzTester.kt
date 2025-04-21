package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.utils.createRandomAction
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail

/**
 * This class can be used to fuzz-test the rule engine by running a lot of
 * games with random actions and pre-defined seeds; that makes it possible to
 * test random paths through the engine as well as being able to reproduce
 * any crashes.
 *
 * For now, this class has to be run manually.
 **/
@Ignore // Comment out to run
class FuzzTester {

    @Test
    fun runRandomGames() {
        val games = 100
        repeat(games) { gameNo ->
            val seed = Random.nextLong()
            val random = Random(seed)
            val state = createDefaultGameState(StandardBB2020Rules())
            val controller = GameEngineController(state)
            controller.startManualMode(logAvailableActions = false)
            try {
                while (controller.stack.isNotEmpty()) {
                    val availableActions = controller.getAvailableActions()
                    val userAction = getSetupAction(controller) ?: createRandomAction(state, availableActions.actions, random)
                    controller.handleAction(userAction)
                }
            } catch (e: Exception) {
                fail("Game $gameNo (seed: $seed) crashed with exception:\n${e.stackTraceToString()}")
            }
        }

    }

    private fun getSetupAction(controller: GameEngineController): GameAction?  {
        val state = controller.state
        val stack = controller.state.stack
        return if (!stack.isEmpty() && stack.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            val context = state.getContext<SetupTeamContext>()
            val compositeActions = mutableListOf<GameAction>()
            if (context.team.isHomeTeam()) {
                handleManualHomeKickingSetup(controller, compositeActions)
            } else {
                handleManualAwayKickingSetup(controller, compositeActions)
            }
            compositeActions.add(EndSetup)
            return CompositeGameAction(compositeActions)
        } else {
            null
        }
    }

    private fun handleManualHomeKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.homeTeam

        val setup = listOf(
            FieldCoordinate(12, 6),
            FieldCoordinate(12, 7),
            FieldCoordinate(12, 8),
            FieldCoordinate(10, 1),
            FieldCoordinate(10, 4),
            FieldCoordinate(10, 10),
            FieldCoordinate(10, 13),
            FieldCoordinate(8, 1),
            FieldCoordinate(8, 4),
            FieldCoordinate(8, 10),
            FieldCoordinate(8, 13),
        )
        setupTeam(team, compositeActions, setup)
    }

    private fun handleManualAwayKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.awayTeam

        val setup = listOf(
            FieldCoordinate(13, 6),
            FieldCoordinate(13, 7),
            FieldCoordinate(13, 8),
            FieldCoordinate(15, 1),
            FieldCoordinate(15, 4),
            FieldCoordinate(15, 10),
            FieldCoordinate(15, 13),
            FieldCoordinate(17, 1),
            FieldCoordinate(17, 4),
            FieldCoordinate(17, 10),
            FieldCoordinate(17, 13),
        )

        setupTeam(team, compositeActions, setup)
    }

    private fun setupTeam(team: Team, compositeActions: MutableList<GameAction>, setup: List<FieldCoordinate>) {
        val playersTaken = mutableSetOf<PlayerId>()

        setup.forEach { fieldCoordinate: FieldCoordinate ->
            team.firstOrNull {
                val inReserve = (it.location == DogOut && it.state == PlayerState.RESERVE)
                inReserve && !playersTaken.contains(it.id)
            }?.let { selectedPlayer ->
                playersTaken.add(selectedPlayer.id)
                compositeActions.add(PlayerSelected(team[selectedPlayer.number]))
                compositeActions.add(FieldSquareSelected(fieldCoordinate))
            }
        }
    }
}
