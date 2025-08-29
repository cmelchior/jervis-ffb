package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Set a square indicator for the square where a ball went out of bounds.
 */
object BallExitStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        // We add a special indicator where the ball is leaving the pitch (if it is)
        state.balls.forEach { ball ->
            ball.outOfBoundsAt?.let { loc ->
                acc.updateSquare(loc) {
                    it.copy(isBallExiting = true)
                }
            }
        }
    }
}
