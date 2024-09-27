package dk.ilios.jervis.procedures.actions.block.standard

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.BlockDicePool
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
import dk.ilios.jervis.rules.Rules

/**
 * Roll block dice for the first time.
 *
 * @see [dk.ilios.jervis.procedures.actions.block.MultipleBlockAction]
 * @see [dk.ilios.jervis.procedures.actions.block.StandardBlockStep]
 */
object StandardBlockChooseResult: Procedure() {
    override val initialNode: Node = SelectBlockResult
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object SelectBlockResult : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(
                SelectDicePoolResult(BlockDicePool(state.getContext<BlockContext>().roll))
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDicePool<DBlockResult>(action) { selectedDie ->
                val context = state.getContext<BlockContext>()
                var selectedIndex = -1
                for (i in context.roll.indices) {
                    // This might select another index if two dice have the same value
                    // Does it matter?
                    if (context.roll[i].result == selectedDie) {
                        selectedIndex = i
                        break
                    }
                }
                compositeCommandOf(
                    SetContext(context.copy(resultIndex = selectedIndex)),
                    ExitProcedure()
                )
            }
        }
    }
}
