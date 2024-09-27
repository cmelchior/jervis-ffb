package dk.ilios.jervis.procedures.actions.block.multipleblock

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectDicePoolResult
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.MultipleBlockContext
import dk.ilios.jervis.rules.Rules

/**
 * Given a
 *
 * @see [dk.ilios.jervis.procedures.actions.block.MultipleBlockAction]
 * @see [dk.ilios.jervis.procedures.actions.block.StandardBlockStep]
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
