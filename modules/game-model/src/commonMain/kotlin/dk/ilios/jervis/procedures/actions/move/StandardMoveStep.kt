package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerMoveLeft
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Handle a player moving a single step using a normal move.
 *
 * This sub procedure is purely used by [MoveTypeSelectorStep], which is also
 * responsible for controlling the lifecycle of [MoveContext].
 */
object StandardMoveStep: Procedure() {
    override val initialNode: Node = SelectTargetSquareOrCancelStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectTargetSquareOrCancelStep: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val player = state.moveContext!!.player
            val eligibleSquares = calculateOptionsForMoveType(state, rules, player, MoveType.STANDARD)
            return eligibleSquares + listOf(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            // TODO Check rolling for dodge
            return checkType<FieldSquareSelected>(action) {
                val moveContext = state.moveContext!!
                val movingPlayer = moveContext.player
                compositeCommandOf(
                    SetPlayerMoveLeft(movingPlayer, movingPlayer.movesLeft - 1),
                    SetPlayerLocation(movingPlayer, it.coordinate),
                    ExitProcedure()
                )
            }
        }

    }
}
