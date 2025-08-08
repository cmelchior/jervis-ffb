package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStepInitialMoveSequence
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator

/**
 * Show intermediate arrows during a push sequence, i.e. directions
 * already selected.
 *
 * The current push is handled by [SelectDirectionDecorator].
 */
object DirectionArrowIndicator: FieldIndicator {
    override fun decorate(
        uiSnapshot: UiGameSnapshot,
        node: ActionNode,
        state: Game,
        request: ActionRequest
    ) {
        state.getContextOrNull<PushContext>()?.let { context ->
            // We only want to show direction arrows when creating the push chain, once the push
            // chain is created and players have moved (and we are about to resolve following up and
            // bouncing balls etc.) they should be removed again. Otherwise the UI gets too confusing.
            val stack = state.stack
            if (
                !stack.containsProcedure(PushStepInitialMoveSequence)
                || state.stack.currentNode() == PushStepInitialMoveSequence.DecideToFollowUp
            ) {
                return@let
            }

            // Only show arrows on intermediate push steps, not the final square
            context.pushChain.forEachIndexed { index, pushData ->
                if (index < context.pushChain.size - 1) {
                    val direction = Direction.from(pushData.from, pushData.to!!)
                    uiSnapshot.fieldSquares[pushData.to]?.directionSelected = direction
                }
            }
        }
    }
}
