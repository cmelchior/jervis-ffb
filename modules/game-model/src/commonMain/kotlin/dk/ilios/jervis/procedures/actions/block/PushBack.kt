package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.reports.ReportPushResult
import dk.ilios.jervis.rules.Rules


// Helper method for creating a push context before moving a player back
// This is used by all results that push back.
fun createPushContext(state: Game): PushContext {
    val blockContext = state.getContext<BlockContext>()
    // Setup the context needed to resolve the full push include
    val newContext = PushContext(
        blockContext.attacker,
        blockContext.defender,
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

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val newContext = createPushContext(state)
        return SetContext(newContext)
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<PushContext>()
        return compositeCommandOf(
            RemoveContext<PushContext>(),
            ReportPushResult(context.firstPusher, context.pushChain.first().from)
        )
    }

    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Target is still standing after a pushback. Any injuries due to being
            // pushed into the crowd are handled in PushStep, so here we just exit.
            return ExitProcedure()
        }
    }
}
