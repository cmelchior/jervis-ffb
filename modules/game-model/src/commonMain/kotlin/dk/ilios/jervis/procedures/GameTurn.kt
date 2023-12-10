package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetTurnNo
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.logs.ReportEndingTurn
import dk.ilios.jervis.logs.ReportStartingTurn
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure controlling the current teams turn.
 */
object GameTurn : Procedure() {
    override val initialNode: Node = WaitForAction

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val turn = state.activeTeam.turnData.currentTurn + 1
        return compositeCommandOf(
            SetTurnNo(state.activeTeam, turn),
            ReportStartingTurn(state.activeTeam, turn)
        )
    }

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

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return ReportEndingTurn(state.activeTeam, state.activeTeam.turnData.currentTurn)
    }
}