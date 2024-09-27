package dk.ilios.jervis.procedures.actions.block.standard

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
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
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules

/**
 * Roll block dice for the first time.
 *
 * @see [dk.ilios.jervis.procedures.actions.block.MultipleBlockAction]
 * @see [dk.ilios.jervis.procedures.actions.block.StandardBlockStep]
 */
object StandardBlockRollDice: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val noOfDice = calculateNoOfBlockDice(state)
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<DBlockResult>(action) { it: List<DBlockResult> ->
                val roll =
                    it.map { diceRoll: DBlockResult ->
                        BlockDieRoll(originalRoll = diceRoll)
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
