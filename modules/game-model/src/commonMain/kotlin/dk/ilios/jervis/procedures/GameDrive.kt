package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetBallLocation
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetCurrentBall
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.reports.ReportSetupKickingTeam
import dk.ilios.jervis.reports.ReportSetupReceivingTeam
import dk.ilios.jervis.reports.ReportStartingKickOff
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
                        SetActiveTeam(state.inactiveTeam),
                        SetKickingTeam(state.receivingTeam),
                        SetTurnOver(null),
                        GotoNode(ResolveEndOfDrive)
                    )
                }
                swapTeams -> {
                    compositeCommandOf(
                        SetActiveTeam(state.inactiveTeam),
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
