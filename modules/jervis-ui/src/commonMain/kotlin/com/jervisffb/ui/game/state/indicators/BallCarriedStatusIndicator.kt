package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Set a square indicator for the ball being carried by the player in that square.
 */
object BallCarriedStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        state.balls.forEach { ball ->
            if (ball.location.isOnField(state.rules)) {
                state.field[ball.location].let { fieldSquare ->
                    val ballCarried = (fieldSquare.player?.hasBall() == true)
                    acc.updateSquare(ball.location) {
                        it.copy(isBallCarried = ballCarried)
                    }
                }
            }
        }
    }
}
