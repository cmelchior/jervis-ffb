package com.jervisffb.engine.rules.bb2025.procedures

import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportTouchback
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * In BB2025, if there are no standing players available to receive a touchback,
 * the ball must be placed on an unoccupied square. This is different from
 * BB2020 where it was given to a prone player (and then bounced).
 */
object BB2025TheKickOffEvent {
    object TouchBack : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val standingPlayers = mutableListOf<Player>()
            state.receivingTeam.forEach {
                if (it.location.isOnField(rules) && it.state == PlayerState.STANDING) {
                    standingPlayers.add(it)
                }
            }

            if (standingPlayers.isNotEmpty()) {
                return listOf(SelectPlayer.fromPlayers(standingPlayers))
            } else {
                val freeSquares: List<TargetSquare> =
                    state.field
                        .filter { rules.isInSetupArea(state.receivingTeam, it) }
                        .filter { it.isUnoccupied() }
                        .map { TargetSquare.setup(it.coordinates) }
                return listOf(SelectFieldLocation(freeSquares))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        SetBallState.carried(state.singleBall(), player),
                        ReportTouchback.fromPlayer(player),
                        ExitProcedure(),
                    )
                }
                is FieldSquareSelected -> {
                    compositeCommandOf(
                        SetBallLocation(state.singleBall(), action.coordinate),
                        SetBallState.onGround(state.singleBall()),
                        ReportTouchback.fromSquare(action.coordinate),
                        ExitProcedure(),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }
}
