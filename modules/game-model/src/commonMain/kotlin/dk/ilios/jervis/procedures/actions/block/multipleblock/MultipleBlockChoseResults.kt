package dk.ilios.jervis.procedures.actions.block.multipleblock

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectDiceResult
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
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Roll block dice for the first time.
 *
 * @see [dk.ilios.jervis.procedures.actions.block.MultipleBlockStep]
 * @see [dk.ilios.jervis.procedures.actions.block.StandardBlockStep]
 */
object MultipleBlockChoseResults: Procedure() {
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
                SelectDiceResult(state.getContext<BlockContext>().roll.map { it.result }, 1)
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<DBlockResult>(action) {
                val selectedDie = when (action) {
                    is DBlockResult -> action
                    is DiceResults -> action.rolls.first() as DBlockResult
                    else -> INVALID_ACTION(action)
                }

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
