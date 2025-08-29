package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Set a square indicator for a ball being in square but not carried by a player.
 */
object BallOnGroundStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        state.balls.forEach { ball ->
            // TODO Something in these checks have the wrong logic. We should not check both "rules state" and "acc" state.
            val isOnGround = (ball.state != BallState.CARRIED && ball.state != BallState.OUT_OF_BOUNDS)
            if (acc.squares.contains(ball.location)) {
                acc.updateSquare(ball.location) {
                    it.copy(isBallOnGround = isOnGround)
                }
            }
        }
    }
}
