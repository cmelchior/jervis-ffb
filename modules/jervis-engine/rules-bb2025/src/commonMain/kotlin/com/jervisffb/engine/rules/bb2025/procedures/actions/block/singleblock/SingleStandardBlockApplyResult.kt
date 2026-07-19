package com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025BothDown
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PlayerDown
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025Pow
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025Stumble

/**
 * Resolve the chosen block result.
 */
object SingleStandardBlockApplyResult: Procedure() {
    override val initialNode: Node = ResolveBlockDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object ResolveBlockDie : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            // Select sub procedure based on the result of the die.
            return when (state.getContext<BlockContext>().result.blockResult) {
                BlockDice.PLAYER_DOWN -> BB2025PlayerDown
                BlockDice.BOTH_DOWN -> BB2025BothDown
                BlockDice.PUSH_BACK -> BB2025PushBack
                BlockDice.STUMBLE -> BB2025Stumble
                BlockDice.POW -> BB2025Pow
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // Once the block die is resolved, this part of the block is over.
            // Standard Block Actions will quit immediately. Blitz actions
            // might allow further movement and Multiblock actions will
            // also continue their lock-step progress.
            return ExitProcedure()
        }
    }
}
