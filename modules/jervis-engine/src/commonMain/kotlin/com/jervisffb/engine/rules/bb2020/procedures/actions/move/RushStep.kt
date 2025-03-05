package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.calculateOptionsForMoveType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Handle a player rushing a single square.
 * See page XX in the rulebook.
 *
 * This sub procedure is purely used by [ResolveMoveTypeStep], which is also
 * responsible for controlling the lifecycle of [MoveContext].
 */
object RushStep: Procedure() {
    override val initialNode: Node = SelectTargetSquareOrCancelStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectTargetSquareOrCancelStep: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val player = state.getContext<MoveContext>().player
            val eligibleSquares = calculateOptionsForMoveType(state, rules, player, MoveType.STANDARD)
            return eligibleSquares + listOf(CancelWhenReady, EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
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
