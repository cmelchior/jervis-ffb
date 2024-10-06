package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.KnockedDown
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.reports.ReportPlayerDownResult
import com.jervisffb.engine.rules.Rules

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
