package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportPrayersOfNuffleRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.TableResult

/**
 * Run a roll on the Prayers of Nuffle table.
 * See page 39  in the rulebook.
 */
object RollOnPrayersOfNuffleTable: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDie: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D16))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D16Result>(action) { d16 ->
                val result: TableResult = rules.prayersToNuffleTable.roll(d16)
                compositeCommandOf(
                    ReportPrayersOfNuffleRoll(state.activeTeam, d16, result),
                    GotoNode(ApplyTableResult(result.procedure)),
                )
            }
        }
    }

    class ApplyTableResult(val procedure: Procedure): ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = procedure
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

}