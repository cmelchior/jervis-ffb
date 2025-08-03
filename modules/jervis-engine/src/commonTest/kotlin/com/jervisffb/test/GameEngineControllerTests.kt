package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.FanFactorRolls
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.utils.InvalidActionException
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

    private fun createGameController(rules: Rules): GameEngineController {
        val state = createDefaultGameState(rules)
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
        return controller
    }

    @Test
    fun undoIncrementActionId() {
        val rules = StandardBB2020Rules().toBuilder().run {
            undoActionBehavior = UndoActionBehavior.ALLOWED
            build()
        }
        val controller = createGameController(rules)

        // Verify that undoing actions keep incrementing the delta id
        assertEquals(0, controller.currentActionIndex().value)
        controller.handleAction(1.d3)
        assertEquals(1, controller.currentActionIndex().value)
        controller.handleAction(2.d3)
        assertEquals(2, controller.currentActionIndex().value)
        assertTrue(controller.isUndoAvailable(controller.state.awayTeam.id))
        controller.handleAction(Undo)
        assertEquals(3, controller.currentActionIndex().value)
        controller.handleAction(Undo)
        assertEquals(4, controller.currentActionIndex().value)
        controller.handleAction(6.d3)
        assertEquals(5, controller.currentActionIndex().value)
    }

    @Test
    fun cannotUndoDiceRollsIfNotInEnabled() {
        val rules = StandardBB2020Rules().toBuilder().run {
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS
            build()
        }
        val controller = createGameController(rules)

        // Verify that undoing actions keep incrementing the delta id
        assertEquals(0, controller.currentActionIndex().value)
        controller.handleAction(1.d3)
        assertEquals(1, controller.currentActionIndex().value)
        assertFalse(controller.isUndoAvailable(controller.state.homeTeam.id))
        assertFailsWith<InvalidActionException> {
            controller.handleAction(Undo)
        }
        assertEquals(1, controller.currentActionIndex().value)
    }

    @Test
    fun revertDecrementsActionId() {
        val rules = StandardBB2020Rules().toBuilder().run {
            undoActionBehavior = UndoActionBehavior.NOT_ALLOWED // Revert is always allowed
            build()
        }
        val controller = createGameController(rules)

        // Verify that reverting actions also decrement the action id
        assertEquals(0, controller.currentActionIndex().value)
        controller.handleAction(1.d3)
        assertEquals(1, controller.currentActionIndex().value)
        controller.handleAction(2.d3)
        assertEquals(2, controller.currentActionIndex().value)
        controller.handleAction(Revert)
        assertEquals(1, controller.currentActionIndex().value)
        controller.handleAction(Revert)
        assertEquals(0, controller.currentActionIndex().value)
    }

    @Test
    fun undoCompositeCommandsUndoAll() {
        val rules = StandardBB2020Rules().toBuilder().run {
            undoActionBehavior = UndoActionBehavior.ALLOWED // Revert is always allowed
            build()
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
}
