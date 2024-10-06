package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Roll block dice for the first time.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.MultipleBlockAction]
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.StandardBlockStep]
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
                if (selectedIndex == -1) {
                    INVALID_GAME_STATE("No matching roll for $selectedDie: ${context.roll}")
                }
                compositeCommandOf(
                    SetContext(context.copy(resultIndex = selectedIndex)),
                    ExitProcedure()
                )
            }
        }
    }
}
