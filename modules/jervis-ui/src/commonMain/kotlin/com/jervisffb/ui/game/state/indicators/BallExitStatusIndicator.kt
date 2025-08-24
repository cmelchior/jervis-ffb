package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot

/**
 * Set a square indicator for the square where a ball went out of bounds.
 */
object BallExitStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        // We add a special indicator where the ball is leaving the pitch (if it is)
        state.balls.forEach { ball ->
            ball.outOfBoundsAt?.let {loc ->
                uiSnapshot.fieldSquares[loc]!!.isBallExiting = true
            }
        }
    }
}
