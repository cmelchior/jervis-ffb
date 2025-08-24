package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot

/**
 * Set a square indicator for a ball being in square but not carried by a player.
 */
object BallOnGroundStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        state.balls.forEach { ball ->
            val isOnGround = (ball.state != BallState.CARRIED && ball.state != BallState.OUT_OF_BOUNDS)
            uiSnapshot.fieldSquares[ball.location]?.isBallOnGround = isOnGround
        }
    }
}
