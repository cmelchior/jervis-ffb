package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiSnapshotAccumulator

/**
 * Set a square indicator for the ball being carried by the player in that square.
 */
object BallCarriedStatusIndicator: PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        state.balls.forEach { ball ->
            if (ball.coordinates.isOnPitch(state.rules)) {
                state.pitch[ball.coordinates].let { pitchSquare ->
                    val ballCarried = (pitchSquare.player?.hasBall() == true)
                    acc.updateSquare(ball.coordinates) {
                        it.copy(isBallCarried = ballCarried)
                    }
                }
            }
        }
    }
}
