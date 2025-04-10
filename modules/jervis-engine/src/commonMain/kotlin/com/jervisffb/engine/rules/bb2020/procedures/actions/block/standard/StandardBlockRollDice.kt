package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRollList
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import kotlin.math.absoluteValue

/**
 * Roll block dice for the first time.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.MultipleBlockAction]
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.StandardBlockStep]
 */
object StandardBlockRollDice: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val noOfDice = state.getContext<BlockContext>().calculateNoOfBlockDice().absoluteValue
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<DBlockResult>(action) { it: List<DBlockResult> ->
                val roll =
                    it.map { diceRoll: DBlockResult ->
                        BlockDieRoll.create(state, diceRoll)
                    }
                return compositeCommandOf(
                    ReportDiceRoll(roll),
                    SetContext(state.getContext<BlockContext>().copy(roll = roll)),
                    ExitProcedure(),
                )
            }
        }
    }

}
