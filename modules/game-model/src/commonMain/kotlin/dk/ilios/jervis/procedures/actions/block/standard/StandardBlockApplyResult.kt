package dk.ilios.jervis.procedures.actions.block.standard

import dk.ilios.jervis.actions.BlockDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.BothDown
import dk.ilios.jervis.procedures.actions.block.PlayerDown
import dk.ilios.jervis.procedures.actions.block.Pow
import dk.ilios.jervis.procedures.actions.block.PushBack
import dk.ilios.jervis.procedures.actions.block.Stumble
import dk.ilios.jervis.rules.Rules

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
            return when(state.getContext<BlockContext>().result.blockResult) {
                BlockDice.PLAYER_DOWN -> PlayerDown
                BlockDice.BOTH_DOWN -> BothDown
                BlockDice.PUSH_BACK -> PushBack
                BlockDice.STUMBLE -> Stumble
                BlockDice.POW -> Pow
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
