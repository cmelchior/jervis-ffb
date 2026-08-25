package com.jervisffb.engine.bb2020.procedures.actions.block

import com.jervisffb.engine.bb2020.procedures.table.injury.KnockedDown2020
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.reports.ReportPlayerDownResult
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules

/**
 * Resolve a "Player Down!" selected as a block result.
 * See page 57 in the rulebook.
 */
object PlayerDown2020: Procedure() {
    override val initialNode: Node = ResolvePlayerDown
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<BlockContext>()
        val injuryContext =
            RiskingInjuryContext(
                player = context.attacker,
                causedBy = context.defender,
                isPartOfMultipleBlock = context.isUsingMultiBlock
            )
        return AddContext(injuryContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportPlayerDownResult(state.getContext<BlockContext>().attacker)
    }

    object ResolvePlayerDown: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown2020
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
