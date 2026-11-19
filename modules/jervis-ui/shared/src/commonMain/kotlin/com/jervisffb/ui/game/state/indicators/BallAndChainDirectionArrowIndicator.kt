package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.bb2025.context.BallAndChainContext
import com.jervisffb.engine.bb2025.procedures.actions.ballandchain.BallAndChainDirectionRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.ui.game.UiSnapshotAccumulator

/** Show the rolled direction while the coach decides whether to re-roll it. */
object BallAndChainDirectionArrowIndicator : PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        if (!state.stack.containsProcedure(BallAndChainDirectionRoll)) return
        val context = state.getContextOrNull<BallAndChainContext>() ?: return
        val destination = context.destination ?: return
        val direction = Direction.from(context.origin, destination)
        // Crowd squares are not rendered; show an outward arrow on the origin instead.
        val arrowSquare = when (destination.isOnPitch(state.rules)) {
            true -> destination
            false -> context.origin
        }
        acc.updateSquare(arrowSquare) {
            it.copy(directionSelected = direction)
        }
    }
}
