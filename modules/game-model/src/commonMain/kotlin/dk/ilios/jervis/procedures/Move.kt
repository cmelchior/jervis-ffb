package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetMoveStepTarget
import dk.ilios.jervis.commands.SetPlayerMoveLeft
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Procedure controlling a Move action as described on page XX in the rulebook.
 */
object Move: Procedure() {
    override val initialNode: Node = SelectSquareOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        return SetPlayerMoveLeft(state.activePlayer!!, state.activePlayer!!.move)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
    }

    object SelectSquareOrEndAction: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val end: List<ActionDescriptor> = listOf(EndActionWhenReady)

            val eligibleEmptySquares: List<ActionDescriptor> = if (state.activePlayer!!.moveLeft > 0) {
                state.activePlayer!!.location.coordinate.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isEmpty() }
                    .map { SelectFieldLocation(it) }
            } else emptyList()

            val eligibleJumpSquares: List<ActionDescriptor> = if (state.activePlayer!!.moveLeft > 0) {
                val activePlayerLocation = state.activePlayer!!.location.coordinate
                activePlayerLocation.getSurroundingCoordinates(rules)
                    .filter { !state.field[it].isEmpty() }
                    .flatMap {
                        it.getCoordinatesAwayFromLocation(rules, activePlayerLocation)
                    }
                    .toSet()
                    .map { SelectFieldLocation(it) }
            } else emptyList()
            return end + eligibleEmptySquares + eligibleJumpSquares
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                EndAction -> ExitProcedure()
                // TODO How to tell the difference between Move and Leap here?
                is FieldSquareSelected -> compositeCommandOf(
                    SetMoveStepTarget(state.activePlayer!!.location.coordinate, FieldCoordinate(action.x, action.y)),
                    GotoNode(MoveToSquare)
                )
                else -> INVALID_ACTION(action)
            }
        }
    }

    object JumpToSquare: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = JumpStep
        override fun onExitNode(state: Game, rules: Rules): Command = GotoNode(SelectSquareOrEndAction)
    }

    object MoveToSquare: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MoveStep
        override fun onExitNode(state: Game, rules: Rules): Command = GotoNode(SelectSquareOrEndAction)
    }
}