package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportTouchback
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.TheKickOffEvent.BounceFromPronePlayer
import com.jervisffb.engine.utils.INVALID_GAME_STATE

object BB2020TheKickOffEvent {
    /**
     * In BB2020, if there are no standing players available to receive a touchback, a prone
     * player/stunned player must be selected instead.
     */
    object TouchBack : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Prone / Stunned players can only be selected if no standing players are available.
            // In that case, it will bounce from their square.
            val standingPlayers = mutableListOf<Player>()
            val pronePlayers = mutableListOf<Player>()
            state.receivingTeam.forEach {
                if (it.location.isOnPitch(rules)) {
                    when (it.state) {
                        PlayerState.PRONE, PlayerState.STUNNED -> {
                            pronePlayers.add(it)
                        }
                        PlayerState.STANDING -> {
                            standingPlayers.add(it)
                        }
                        else -> INVALID_GAME_STATE("Unsupported state: ${it.state}")
                    }
                }
            }
            return if (standingPlayers.isEmpty()) {
                listOf(SelectPlayer.fromPlayers(pronePlayers))
            } else {
                listOf(SelectPlayer.fromPlayers(standingPlayers))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<PlayerSelected>(action) {
                val player = it.getPlayer(state)
                if (player.state == PlayerState.STANDING) {
                    return compositeCommandOf(
                        SetBallState.carried(state.singleBall(), player),
                        ReportTouchback.fromPlayer(player),
                        ExitProcedure(),
                    )
                } else {
                    compositeCommandOf(
                        // TODO Giant Support
                        SetBallState.carried(state.singleBall(), player),
                        SetBallLocation(state.singleBall(), player.coordinates),
                        SetBallState.bouncing(state.singleBall()),
                        ReportTouchback.fromPronePlayer(player),
                        GotoNode(BounceFromPronePlayer)
                    )
                }
            }
        }
    }
}
