package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetRollContext
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportFollowup
import dk.ilios.jervis.rules.Rules

/**
 * Resolve a pushback when select on a block die.
 */
object PushBack: Procedure() {
    override val initialNode: Node = ResolvePush

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val blockContext = state.blockRollResultContext!!
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
        return SetRollContext(Game::pushContext, newContext)
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.pushContext!!
        return compositeCommandOf(
            SetRollContext(Game::pushContext, null),
            ReportFollowup(context.pusher, context.pushChain.first().from)
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
