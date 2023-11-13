package dk.ilios.bowlbot.procedures

import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.EndTurn
import dk.ilios.bowlbot.actions.EndTurnWhenReady
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.fsm.ActionNode
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

/**
 * Procedure controlling the current teams turn.
 */
object GameTurn : Procedure {
    override val initialNode: Node = WaitForAction

    object WaitForAction: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(EndTurnWhenReady)
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return checkType<EndTurn>(action) { action: EndTurn ->
                ExitProcedure()
            }
        }
    }
}