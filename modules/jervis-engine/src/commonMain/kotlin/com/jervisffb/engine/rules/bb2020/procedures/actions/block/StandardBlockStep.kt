package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockApplyResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseReroll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockChooseResult
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockDetermineModifiers
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRerollDice
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard.StandardBlockRollDice
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling a standard block once attacker and defender have been
 * identified. This includes rolling dice and resolving the result.
 *
 * This procedure is called as part of a [BlockAction] or [BlitzAction].
 */
object StandardBlockStep : Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = UseRerollContext(type = DiceRollType.BLOCK)
        return AddContext(context)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.rerollContext
        if (context?.type != DiceRollType.BLOCK) {
            INVALID_GAME_STATE("Invalid reroll type for SingleStandardBlockStep: ${context?.type}")
        }
        return RemoveContext(context)
    }
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
            return when (state.rerollContext?.source != null) {
                true -> GotoNode(RerollDice)
                false -> GotoNode(SelectBlockResult)
            }
        }
    }

    object RerollDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockContext>()
            // If all dice have been rerolled, we know for sure the final result. Otherwise, some skill might
            // allow further rerolls, so
            return when (rules.isRerollAllowed(context.roll)) {
                true -> GotoNode(SelectRerollType)
                false -> GotoNode(SelectBlockResult)
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
