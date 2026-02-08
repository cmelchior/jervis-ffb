package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockApplyResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockDetermineModifiers
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice

/**
 * Procedure for handling a standard block once attacker and defender have been identified. This includes
 * rolling dice and resolving the result.
 *
 * This procedure is called as part of a [BlockAction] or [BlitzAction].
 */
object StandardBlockStep : Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object DetermineAssists: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockDetermineModifiers
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RollBlockDice)
        }
    }

    object RollBlockDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockRollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectRerollType)
        }
    }

    object SelectRerollType : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockChooseReroll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.rerollContext != null) {
                GotoNode(RerollDice)
            } else {
                // We can select multiple rerolls
                GotoNode(SelectBlockResult)
            }
        }
    }

    object RerollDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            // If all dice have been rerolled, we know for sure the final result. Otherwise, some skill might
            // allow further rerolls, so
            return if (rules.isRerollAllowed(context.roll)) {
                GotoNode(SelectBlockResult)
            } else {
                GotoNode(SelectRerollType)
            }
        }
    }

    object SelectBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockChooseResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBlockResult)
        }
    }

    object ResolveBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockApplyResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Once the block die is resolved, the block step is over
            // and all injuries have been resolved
            return ExitProcedure()
        }
    }
}
