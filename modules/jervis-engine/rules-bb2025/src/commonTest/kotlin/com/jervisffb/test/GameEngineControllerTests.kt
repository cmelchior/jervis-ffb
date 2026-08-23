package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.common.procedures.FanFactorRolls
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.common.procedures.WeatherRoll
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertNoActivePlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Controller tests that don't fit in a more specific category.
 */
class GameEngineControllerTests {

    private lateinit var controller: GameEngineController

    private fun createGameController(rules: Rules, collectStatistics: Boolean = false): GameEngineController {
        val state = createDefaultGameStateBB2025(rules)
        controller = GameEngineController(
            state = state,
            statistics = if (collectStatistics) GameStatistics() else null,
        )
        controller.startTestMode(FullGame)
        return controller
    }

    @Test
    fun undoIncrementActionId() {
        val rules = StandardBB2025Rules().update {
            undoActionBehavior = UndoActionBehavior.ALLOWED
        }
        val controller = createGameController(rules)

        // Verify that undoing actions keep incrementing the delta id
        assertEquals(0, controller.currentActionId().counter)
        controller.handleAction(1.d3)
        assertEquals(1, controller.currentActionId().counter)
        controller.handleAction(2.d3)
        assertEquals(2, controller.currentActionId().counter)
        assertTrue(controller.isUndoAvailable(controller.state.awayTeam.id))
        controller.handleAction(Undo)
        assertEquals(3, controller.currentActionId().counter)
        controller.handleAction(Undo)
        assertEquals(4, controller.currentActionId().counter)
        controller.handleAction(2.d3)
        assertEquals(5, controller.currentActionId().counter)
    }

    @Test
    fun cannotUndoDiceRollsIfNotInEnabled() {
        val rules = StandardBB2025Rules().update {
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS
        }
        val controller = createGameController(rules)

        // Verify that undoing actions keep incrementing the delta id
        assertEquals(0, controller.currentActionId().counter)
        controller.handleAction(1.d3)
        assertEquals(1, controller.currentActionId().counter)
        assertFalse(controller.isUndoAvailable(controller.state.homeTeam.id))
        assertFailsWith<InvalidActionException> {
            controller.handleAction(Undo)
        }
        assertEquals(1, controller.currentActionId().counter)
    }

    @Test
    fun revertStartsNewActionIdGeneration() {
        val rules = StandardBB2025Rules().update {
            undoActionBehavior = UndoActionBehavior.NOT_ALLOWED // Revert is always allowed
        }
        val controller = createGameController(rules)

        // Verify that reverting actions decrements the counter and starts a new generation.
        assertEquals(GameActionId.INITIAL, controller.currentActionId())
        controller.handleAction(1.d3)
        assertEquals(GameActionId(1, 1), controller.currentActionId())
        controller.handleAction(2.d3)
        assertEquals(GameActionId(2, 1), controller.currentActionId())
        controller.handleAction(Revert)
        assertEquals(GameActionId(1, 2), controller.currentActionId())
        controller.handleAction(Revert)
        assertEquals(GameActionId(0, 3), controller.currentActionId())
    }

    @Test
    fun gameActionIdComparesGenerationBeforeCounter() {
        assertTrue(GameActionId(1, 1) > GameActionId(100, 0))
        assertTrue(GameActionId(1, 0) < GameActionId(2, 0))
        assertEquals(GameActionId(3, 4), GameActionId(2, 4).next())
    }

    @Test
    fun undoCompositeCommandsUndoAll() {
        val rules = StandardBB2025Rules().update {
            undoActionBehavior = UndoActionBehavior.ALLOWED // Revert is always allowed
        }
        val controller = createGameController(rules)
        assertEquals(FanFactorRolls.SetFanFactorForHomeTeam, controller.currentNode())
        controller.handleAction(
            CompositeGameAction(1.d3, 2.d3)
        )
        assertEquals(WeatherRoll.RollWeatherDice, controller.currentNode())
        controller.handleAction(Undo)
        assertEquals(FanFactorRolls.SetFanFactorForHomeTeam, controller.currentNode())
    }

    @Test
    fun invalidCompositeActionRollsBackAllCompletedSteps() {
        val controller = createGameController(StandardBB2025Rules())
        val state = controller.state
        val nodeBefore = controller.currentNode()
        val stackBefore = controller.stack.stateToPrettyString()
        val requestBefore = controller.getAvailableActions()
        val logsBefore = state.logs.toList()
        val homeFansBefore = state.homeTeam.fairWeatherFans
        val historySizeBefore = controller.history.size
        val actionIdBefore = controller.currentActionId()

        assertFailsWith<InvalidActionException> {
            controller.handleAction(
                CompositeGameAction(
                    1.d3,
                    DirectionSelected(Direction.LEFT),
                ),
            )
        }

        assertEquals(nodeBefore, controller.currentNode())
        assertEquals(stackBefore, controller.stack.stateToPrettyString())
        assertEquals(requestBefore, controller.getAvailableActions())
        assertEquals(logsBefore, state.logs)
        assertEquals(homeFansBefore, state.homeTeam.fairWeatherFans)
        assertEquals(historySizeBefore, controller.history.size)
        assertEquals(actionIdBefore, controller.currentActionId())
    }

    // During Replay, we can both play backwards and forwards using a pre-determined sequence of actions.
    // If the various ID's, especially DiceId, are not correctly incremented and decremented, the game will
    // crash with invalid action errors.
    @Test
    fun undoDiceIdDuringReplay() {
        val rules = StandardBB2025Rules().update {
            undoActionBehavior = UndoActionBehavior.ALLOWED
        }
        val controller = createGameController(rules)

        // Move into a state where we rolled block dice as that has caused problems with diceId
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 4.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false)
        )

        // Save a copy of all actions
        val actions = controller.history.flatMap { delta ->
            delta.steps.map { step -> step.action }
        }

        // Revert all actions back to start
        for (i in actions.indices) {
            controller.handleAction(Undo)
        }
        assertTrue(controller.state.contexts.isEmpty())
        assertEquals(FanFactorRolls.SetFanFactorForHomeTeam, controller.currentNode())

        // Re-apply all actions (similar to what a replay would do)
        actions.forEach { action ->
            controller.handleAction(action)
        }

        val state = controller.state
        assertEquals(state.getPlayerById("H1".playerId).coordinates, PitchCoordinate(11, 5))
        state.awayTeam.assertActive()
        state.assertNoActivePlayer()
    }
}
