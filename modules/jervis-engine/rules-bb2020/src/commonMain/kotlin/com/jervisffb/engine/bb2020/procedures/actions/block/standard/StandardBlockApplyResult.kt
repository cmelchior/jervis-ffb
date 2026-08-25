package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.bb2020.procedures.actions.block.BothDown2020
import com.jervisffb.engine.bb2020.procedures.actions.block.PlayerDown2020
import com.jervisffb.engine.bb2020.procedures.actions.block.Pow2020
import com.jervisffb.engine.bb2020.procedures.actions.block.PushBack2020
import com.jervisffb.engine.bb2020.procedures.actions.block.Stumble2020
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules

/**
 * Resolve the chosen block result.
 */
object StandardBlockApplyResult: Procedure() {
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
                BlockDice.PLAYER_DOWN -> PlayerDown2020
                BlockDice.BOTH_DOWN -> BothDown2020
                BlockDice.PUSH_BACK -> PushBack2020
                BlockDice.STUMBLE -> Stumble2020
                BlockDice.POW -> Pow2020
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
