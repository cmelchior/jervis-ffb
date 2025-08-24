package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.ui.game.UiGameSnapshot

/**
 * Set a square indicator for the ball being carried by the player in that square.
 */
object BallCarriedStatusIndicator: FieldStatusIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        state.balls.forEach {
            if (it.location.isOnField(state.rules)) {
                state.field[it.location].let { fieldSquare ->
                    val ballCarried = (fieldSquare.player?.hasBall() == true)
                    uiSnapshot.fieldSquares[it.location]?.isBallCarried = ballCarried
                }
            }
        }
    }
}
