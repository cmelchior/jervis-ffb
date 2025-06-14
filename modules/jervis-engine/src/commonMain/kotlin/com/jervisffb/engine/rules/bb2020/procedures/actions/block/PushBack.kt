package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.StumbleContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportPushResult
import com.jervisffb.engine.rules.Rules


// Helper method for creating a push context before moving a player back
// This is used by all results that push back.
fun createPushContext(state: Game): PushContext {
    val blockContext = state.getContext<BlockContext>()
    val stumbleContext = state.getContextOrNull<StumbleContext>()
    // TODO Are there any special skills that also knock players down?
    val isKnockedDown = stumbleContext?.isDefenderDown() ?: (blockContext.result.blockResult == BlockDice.POW)

    // Setup the context needed to resolve the full push include
    val newContext = PushContext(
        firstPusher = blockContext.attacker,
        firstPushee = blockContext.defender,
        isDefenderKnockedDown = isKnockedDown,
        blockContext.isUsingMultiBlock,
        mutableListOf(
            PushContext.PushData(
                pusher = blockContext.attacker,
                pushee = blockContext.defender,
                from = blockContext.defender.location as FieldCoordinate,
                isBlitzing = blockContext.isBlitzing,
                isChainPush = false,
                usingJuggernaut = false
            )
        )
    )
    return newContext
}

/**
 * Resolve a pushback when select on a block die.
 * See page 57 in the rulebook.
 *
 * The logic differs slightly depending on this being a single block or part of
 * a multiple block. For multiple block, we only run the first part of the push
 * sequence. The rest is delayed until later.
 */
object PushBack: Procedure() {
    override val initialNode: Node = ResolveInitialPushSequence
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val newContext = createPushContext(state)
        return SetContext(newContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PushContext>()
        return compositeCommandOf(
            RemoveContext<PushContext>(),
            ReportPushResult(context.firstPusher, context.pushChain.first().from, context.followsUp)
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object ResolveInitialPushSequence: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStepInitialMoveSequence
        override fun onExitNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            val defenderHasBall = pushContext.firstPushee.hasBall()
            return buildCompositeCommand {
                add(SetContext(blockContext.copy(didFollowUp = pushContext.followsUp)))
                if (blockContext.isUsingMultiBlock) {
                    val multipleBlockContext = state.getContext<MultipleBlockContext>()
                    val property = if (multipleBlockContext.activeDefender == 0) {
                        MultipleBlockContext::defender1PushChain
                    } else {
                        MultipleBlockContext::defender2PushChain
                    }
                    add(SetContextProperty(property, multipleBlockContext, pushContext))
                }
                // If this is a single block, we can resolve the the full pushe sequence here.
                // If not. We can only do it up to following up, and then leave the rest to
                // be managed by the MultipleBlockAction procedure since the timing there
                // is different from single blocks.
                val navigationCommand = when {
                    defenderHasBall -> GotoNode(DecideToUseStripBall)
                    blockContext.isUsingMultiBlock -> ExitProcedure()
                    else -> GotoNode(ResolveRemainingPushSequenceForSingleBlock)
                }
                add(navigationCommand)
            }
        }
    }

    // TODO
    object DecideToUseStripBall: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val isMultipleBlock = state.hasContext<MultipleBlockContext>()
            return when (isMultipleBlock) {
                true -> ExitProcedure()
                false -> GotoNode(ResolveRemainingPushSequenceForSingleBlock)
            }
        }
    }

    object ResolveRemainingPushSequenceForSingleBlock: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStepResolveSingleBlockPushChain
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
