package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.tables.injury.KnockedDown
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.reports.ReportPlayerDownResult
import dk.ilios.jervis.rules.Rules

/**
 * Resolve a "Player Down!" selected as a block result.
 * See page 57 in the rulebook.
 */
object PlayerDown: Procedure() {
    override val initialNode: Node = ResolvePlayerDown
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<BlockContext>()
        val injuryContext = RiskingInjuryContext(context.attacker, context.isUsingMultiBlock)
        return compositeCommandOf(
            SetTurnOver(TurnOver.STANDARD),
            SetPlayerState(context.attacker, PlayerState.KNOCKED_DOWN, hasTackleZones = false),
            SetContext(injuryContext),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportPlayerDownResult(state.getContext<BlockContext>().attacker)
    }

    object ResolvePlayerDown: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
