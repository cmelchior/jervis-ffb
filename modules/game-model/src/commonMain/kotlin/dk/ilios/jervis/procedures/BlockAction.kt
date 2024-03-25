package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetRollContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Procedure for controlling a player's Block action.
 *
 * See page 56 in the rulebook.
 */
object BlockAction: Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
    }

    object SelectDefenderOrEndAction: ActionNode() {
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
                is PlayerSelected -> {
                    val context = BlockContext(
                        attacker = state.activePlayer!!,
                        defender = action.player
                    )
                    compositeCommandOf(
                        SetRollContext(Game::blockContext, context),
                        GotoNode(ResolveBlock)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveBlock: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}