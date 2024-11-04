package com.jervisffb.test.ext

import com.jervisffb.engine.GameController
import com.jervisffb.engine.GameController.ActionMode
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.Undo

/**
 * Test method. Used to apply multiple [GameAction]s in on go.
 */
fun GameController.rollForward(vararg actions: GameAction?) {
    if (actionMode != ActionMode.TEST) {
        error("Invalid action mode: $actionMode. Must be ActionMode.TEST.")
    }
    actions.forEach {
        if (it != null) {
            val action = if (it is CalculatedAction) it.get(state, rules) else it
            processAction(action)
        }
    }
}

/**
 * Undo a given number of actions already processed
 */
fun GameController.undoActions(actions: Int) {
    repeat(actions) {
        this.processAction(Undo)
    }
}
