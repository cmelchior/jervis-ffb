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
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.ReportEndingTurn
import dk.ilios.jervis.reports.ReportStartingTurn
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for controlling the active teams turn.
 *
 * See page 42 in the rulebook
 */
object TeamTurn : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val turn = state.activeTeam.turnData.currentTurn + 1u
        return compositeCommandOf(
            SetTurnNo(state.activeTeam, turn),
            ReportStartingTurn(state.activeTeam, turn)
        )
    }

    object SelectPlayer: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(EndTurnWhenReady)
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

    object SelectAction: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: Action, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

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

    object ResolveEndOfTurn: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement end-of-turn things
            //  - Players stunned at the beginning of the turn are now prone
            return ExitProcedure()
        }
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return ReportEndingTurn(state.activeTeam, state.activeTeam.turnData.currentTurn)
    }
}