package com.jervisffb.engine.rules.common.procedures

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
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Game drive procedure.
 *
 * This procedure manages the entire game drive, from setting up the teams to executing the kick-off and handling turnovers.
 */
object GameDrive : Procedure() {
    override val initialNode: Node = StartOfDrive
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object StartOfDrive : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StartOfDriveSequence
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(Turn)
        }
    }

    object Turn : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (rules.baseVersion) {
                GameVersion.BB2020 -> com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
                GameVersion.BB2025 -> com.jervisffb.engine.rules.bb2025.procedures.TeamTurn
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val activeTouchdownScored = (state.turnOver == TurnOver.ACTIVE_TEAM_TOUCHDOWN)
            val inactiveTouchdownScored = (state.turnOver == TurnOver.INACTIVE_TEAM_TOUCHDOWN)
            val isOutOfTime = if (state.halfNo <= rules.halfsPrGame) {
                state.homeTeam.turnMarker == rules.turnsPrHalf
                    && state.awayTeam.turnMarker == rules.turnsPrHalf
            } else {
                state.homeTeam.turnMarker == rules.turnsInExtraTime
                    && state.awayTeam.turnMarker == rules.turnsInExtraTime
            }
            val endDrive = activeTouchdownScored || isOutOfTime
            val swapTeams = !isOutOfTime && !activeTouchdownScored

            return when {
                inactiveTouchdownScored -> {
                    compositeCommandOf(
                        // Keep the current turnover state as we use it as an indicator to terminate
                        // next team turn immediately.
                        SetActiveTeam(state.inactiveTeamOrThrow()),
                        SetKickingTeam(state.receivingTeam),
                        GotoNode(Turn)
                    )
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
            // do it here after the sequence has completed. This also includes removing the
            // ball from the field.
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
                // Us having to do this here, indicates we don't fully track the the lifecycle of the current ball
                SetCurrentBall(null),
                *movePlayers.toTypedArray(),
                ExitProcedure(),
            )
        }
    }
}
