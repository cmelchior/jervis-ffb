package com.jervisffb.ui.game.state.indicators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BB2020PushStepInitialMoveSequence
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.FollowUpStep
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator

/**
 * Show intermediate arrows during a push sequence, i.e. directions
 * already selected.
 *
 * The current push is handled by [SelectDirectionDecorator].
 */
object PushDirectionArrowStatusIndicator: PitchStatusIndicator {
    override fun decorate(
        node: ActionNode,
        state: Game,
        request: ActionRequest,
        acc: UiSnapshotAccumulator
    ) {
        state.getContextOrNull<PushContext>()?.let { context ->
            // We only want to show direction arrows when creating the push chain, once the push
            // chain is created and players have moved (and we are about to resolve following up and
            // bouncing balls etc.) they should be removed again. Otherwise the UI gets too confusing.
            val stack = state.stack
            val isBB2020FollowUp = !stack.containsProcedure(BB2020PushStepInitialMoveSequence) || state.stack.currentNode() == BB2020PushStepInitialMoveSequence.DecideToFollowUp
            val isBB2025FollowUp = !stack.containsProcedure(FollowUpStep) || state.stack.currentNode() == FollowUpStep.ChooseToFollowUp
            if (isBB2025FollowUp || isBB2020FollowUp) return@let

            // Only show arrows on intermediate push steps, not the final square
            context.pushChain.forEachIndexed { index, pushData ->
                if (index < context.pushChain.size - 1) {
                    val target = pushData.to!!
                    val direction = Direction.from(pushData.from, target)
                    acc.updateSquare(target) {
                        it.copy(directionSelected = direction)
                    }
                }
            }
        }
    }
}
