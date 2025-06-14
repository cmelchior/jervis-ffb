package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportPushResult
import com.jervisffb.engine.rules.Rules


// Helper method for creating a push context before moving a player back
// This is used by all results that push back.
fun createPushContext(state: Game): PushContext {
    val blockContext = state.getContext<BlockContext>()
    // Setup the context needed to resolve the full push include
    val newContext = PushContext(
        blockContext.attacker,
        blockContext.defender,
        blockContext.isUsingMultiBlock,
        listOf(
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
 */
object PushBack: Procedure() {
    override val initialNode: Node = ResolvePush
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

    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Target is still standing after a pushback. Any injuries due to being
            // pushed into the crowd are handled in PushStep, so here we just exit.
            val blockContext = state.getContext<BlockContext>()
            val pushContext = state.getContext<PushContext>()
            return compositeCommandOf(
                SetContext(blockContext.copy(didFollowUp = pushContext.followsUp)),
                ExitProcedure()
            )
        }
    }
}
