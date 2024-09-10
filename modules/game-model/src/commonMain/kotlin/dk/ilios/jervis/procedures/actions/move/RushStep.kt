package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerRushesLeft
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Handle a player rushing a single square.
 * See page XX in the rulebook.
 *
 * This sub procedure is purely used by [MoveTypeSelectorStep], which is also
 * responsible for controlling the lifecycle of [MoveContext].
 */
object RushStep: Procedure() {
    override val initialNode: Node = SelectTargetSquareOrCancelStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectTargetSquareOrCancelStep: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val player = state.getContext<MoveContext>().player
            val eligibleSquares = calculateOptionsForMoveType(state, rules, player, MoveType.STANDARD)
            return eligibleSquares + listOf(CancelWhenReady, EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                is FieldSquareSelected -> {
                    val moveContext = state.getContext<MoveContext>()
                    val movingPlayer = moveContext.player
                    compositeCommandOf(
                        SetPlayerRushesLeft(movingPlayer, movingPlayer.rushesLeft - 1),
                        SetPlayerLocation(movingPlayer, action.coordinate),
                        ExitProcedure()
                    )
                }
                is Cancel -> ExitProcedure()
                is EndAction -> ExitProcedure() // How to signal end-of-action?
                else -> INVALID_ACTION(action)
            }
        }

    }
}
