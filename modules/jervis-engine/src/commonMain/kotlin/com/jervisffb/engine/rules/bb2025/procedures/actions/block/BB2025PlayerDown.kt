package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.BB2025MultipleBlockContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportPlayerDownResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025KnockedDown
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext

/**
 * Resolve a "Player Down!" selected as a block result.
 *
 * If this is part of a Multiple Block, the attacker is not actually knocked
 * down yet. Only skills that affect "Player Down!" will be handled, and if the
 * player is still knocked down, they will be marked for it, but not actually
 * knocked down yet.
 *
 * See page 62 in the BB2025 rulebook.
 */
object BB2025PlayerDown: Procedure() {
    override val initialNode: Node = CheckForMultipleBlock
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

    object CheckForMultipleBlock: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            val isDuringMultipleBlock = context.isUsingMultiBlock
            return when (isDuringMultipleBlock) {
                true -> {
                    val multiContext = state.getContext<BB2025MultipleBlockContext>()
                    compositeCommandOf(
                        SetContext(multiContext.copy(attackerKnockedDown = true)),
                        ExitProcedure(),
                    )
                }
                false -> GotoNode(ResolvePlayerDown)
            }
        }
    }

    object ResolvePlayerDown: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
