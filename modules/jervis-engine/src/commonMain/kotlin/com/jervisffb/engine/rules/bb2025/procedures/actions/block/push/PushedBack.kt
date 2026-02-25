package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportPushResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.createPushContext

/**
 * Procedure controlling "Pushed Players", "Chain Pushes", "Pushed Into the
 * Crowd" and "Follow-up".
 *
 * Caller of this procedure is responsible for removing the [PushContext].
 *
 * See page 62/63 in the BB2025 rulebook.
 *
 * For single blocks, the entire procedure is run. For Multiple blocks, many
 * of these steps must be run in lockstep. In this case, this procedure
 */
object PushedBack: Procedure() {
    override val initialNode: Node = CreatePushChain
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val newContext = createPushContext(state)
        return SetContext(newContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<PushContext>()
        return compositeCommandOf(
            ReportPushResult(context.firstPusher, context.pushChain.first().from, context.followsUp)
        )
    }

    object CreatePushChain: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure =
            CreatePushChainStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val blockContext = state.getContext<BlockContext>()
            return if (blockContext.isUsingMultiBlock) {
                // For Multiple Block, the different phases are run in lock-step, which
                // is controlled by the MultipleBlockAction
                ExitProcedure()
            }else {
                GotoNode(MovePlayersInPushChain)
            }
        }
    }

    object MovePlayersInPushChain: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return MovePlayersInPushChainStep
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ChooseToFollowUp)
        }
    }

    object ChooseToFollowUp: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return FollowUpStep
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(UseStripBall)
        }
    }

    object UseStripBall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return UseStripBallStep
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveEventsInPushChain)
        }
    }

    object ResolveEventsInPushChain: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return ResolveEventsInPushChainStep
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

//    // TODO
//    object DecideToUseStripBall: ComputationNode() {
//        override fun apply(state: Game, rules: Rules): Command {
//            val isMultipleBlock = state.hasContext<BB2020MultipleBlockContext>()
//            return when (isMultipleBlock) {
//                true -> ExitProcedure()
//                false -> GotoNode(ResolveRemainingPushSequenceForSingleBlock)
//            }
//        }
//    }
//
//    object ResolveRemainingPushSequenceForSingleBlock: ParentNode() {
//        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020PushStepResolveSingleBlockPushChain
//        override fun onExitNode(state: Game, rules: Rules): Command {
//            return ExitProcedure()
//        }
//    }
}
