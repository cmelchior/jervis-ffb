package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetActiveTeam
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetKickingTeam
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportSetupKickingTeam
import com.jervisffb.engine.reports.ReportSetupReceivingTeam
import com.jervisffb.engine.reports.ReportStartingKickOff
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

object GameDrive : Procedure() {
    override val initialNode: Node = SetupKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetupKickingTeam : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetContext(SetupTeamContext(state.kickingTeam)),
                ReportSetupKickingTeam(state.kickingTeam),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SetupReceivingTeam)
        }
    }

    object SetupReceivingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam

        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetContext(SetupTeamContext(state.receivingTeam)),
                ReportSetupReceivingTeam(state.receivingTeam),
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOff)
        }
    }

    object KickOff : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOff
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // Only one ball should exist at kick-off
                SetCurrentBall(state.balls.single()),
                ReportStartingKickOff(state.kickingTeam)
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                GotoNode(KickOffEvent),
            )
        }
    }

    object KickOffEvent : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOffEvent
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                SetActiveTeam(state.receivingTeam),
                GotoNode(Turn),
            )
        }
    }

    object Turn : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TeamTurn
        override fun onExitNode(state: Game, rules: Rules): Command {
            val isTurnOver = (state.turnOver == TurnOver.STANDARD)
            val activeGoalScored = (state.turnOver == TurnOver.ACTIVE_TEAM_TOUCHDOWN)
            // TODO If this is true, we need to run another turn for the team but exit it straight away.
            val inactiveTouchdownScored = (state.turnOver == TurnOver.INACTIVE_TEAM_TOUCHDOWN)
            val isOutOfTime = if (state.halfNo <= rules.halfsPrGame) {
                state.homeTeam.turnMarker == rules.turnsPrHalf &&
                    state.awayTeam.turnMarker == rules.turnsPrHalf
            } else {
                state.homeTeam.turnMarker == rules.turnsInExtraTime &&
                    state.awayTeam.turnMarker == rules.turnsInExtraTime
            }
            val endDrive = activeGoalScored || isOutOfTime
            val swapTeams = !isOutOfTime && !activeGoalScored

            return when {
                inactiveTouchdownScored -> {
                    TODO("Add support for this")
                }
                endDrive -> {
                    compositeCommandOf(
                        SetActiveTeam(null),
                        SetKickingTeam(state.receivingTeam),
                        SetTurnOver(null),
                        GotoNode(ResolveEndOfDrive)
                    )
                }
                swapTeams -> {
                    compositeCommandOf(
                        SetActiveTeam(state.inactiveTeamOrThrow()),
                        SetKickingTeam(state.receivingTeam),
                        SetTurnOver(null),
                        GotoNode(Turn)
                    )
                }
                else -> INVALID_GAME_STATE("Unsupported state")
            }
        }
    }

    object ResolveEndOfDrive : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = EndOfDriveSequence
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The End of Drive Sequence doesn't mention moving players off the pitch, so we
            // do it here after the sequence has completed. This also includes removing th
            val movePlayers: List<Command> = state.field
                .filter { !it.isUnoccupied() }
                .map {
                    val player = it.player!!
                    compositeCommandOf(
                        SetPlayerState(player, PlayerState.RESERVE),
                        SetPlayerLocation(player, DogOut)
                    )
                }
            // At this point, all temporary balls should have been removed.
            return compositeCommandOf(
                SetBallState.onGround(state.getBall()),
                SetBallLocation(state.getBall(), FieldCoordinate.UNKNOWN),
                *movePlayers.toTypedArray(),
                ExitProcedure(),
            )
        }
    }
}
