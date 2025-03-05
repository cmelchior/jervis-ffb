package com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDicePool
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockContext

/**
 * Given a
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.MultipleBlockAction]
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.StandardBlockStep]
 */
object MultipleBlockChoseResults: Procedure() {
    override val initialNode: Node = AttackerSelectBlockResults
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    // TODO It isn't guaranteded that it is the blocker team that selects the dice.
    // We might need to have both attacker and defender choose dice
    object AttackerSelectBlockResults : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MultipleBlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val roll1 = context.roll1!!
            val roll2 = context.roll2!!
            return listOf(
                SelectDicePoolResult(listOf(
                    roll1.createDicePool(id = 0),
                    roll2.createDicePool(id = 1)
                ))
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDicePool<DBlockResult, DBlockResult>(action) { pool1Die, pool2Die ->
                val context = state.getContext<MultipleBlockContext>()
                var updatedRoll1 = context.roll1!!.copyAndSetSelectedResult(pool1Die)
                val updatedRoll2 = context.roll2!!.copyAndSetSelectedResult(pool2Die)
                return compositeCommandOf(
                    SetContext(context.copy(roll1 = updatedRoll1, roll2 = updatedRoll2)),
                    ExitProcedure()
                )
            }
        }
    }
}
