package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetActiveTeam
import com.jervisffb.engine.commands.SetDrive
import com.jervisffb.engine.commands.SetHalf
import com.jervisffb.engine.commands.SetKickingTeamAtHalfTime
import com.jervisffb.engine.commands.SetTurnMarker
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.ReportGameResult
import com.jervisffb.engine.reports.ReportGoingIntoSuddenDeath
import com.jervisffb.engine.reports.ReportStartingDrive
import com.jervisffb.engine.reports.ReportStartingExtraTime
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Duration

/**
 * Procedure responsible for handling Extra Time as described on page 67 in the rulebook.
 */
object ExtraTime : Procedure() {
    override val initialNode: Node = DetermineKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Swap kicking team before entering Extra Time back to the Home team
        // so it emulates starting the game, where it is always the Home Team
        // that is throwing the coin and the Away Team that chooses the result
        var kickingTeam = state.homeTeam
        return compositeCommandOf(
            SetHalf(state.halfNo + 1),
            SetDrive(0),
            SetKickingTeamAtHalfTime(kickingTeam),
            SetActiveTeam(kickingTeam.otherTeam()),
//            ResetAvailableTeamRerolls(state.homeTeam),
//            ResetAvailableTeamRerolls(state.awayTeam),
            SetTurnMarker(state.homeTeam, 0),
            SetTurnMarker(state.awayTeam, 0),
            ReportStartingExtraTime,
        )
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command {
        // Remove modifiers that only last this half
        val resetCommands = getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_HALF)
        return compositeCommandOf(
            *resetCommands
        )
    }

    object DetermineKickingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure =
            com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RunExtraHalf)
        }
    }

    object Drive : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
                ReportStartingDrive(drive),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = GameDrive
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Both teams ran out of time
            return if (state.homeTeam.turnMarker == rules.turnsPrHalf && state.awayTeam.turnMarker == rules.turnsPrHalf) {
                ExitProcedure()
            } else {
                GotoNode(Drive)
            }
        }
    }

    object RunExtraHalf : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
                ReportStartingDrive(drive),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = GameDrive
        override fun onExitNode(state: Game, rules: Rules): Command {
            val outOfTime = (
                state.homeTeam.turnMarker == rules.turnsInExtraTime &&
                    state.awayTeam.turnMarker == rules.turnsInExtraTime
            )
            return when {
                outOfTime && state.homeScore != state.awayScore -> {
                    compositeCommandOf(
                        ReportGameResult(state, extraTime = true, suddenDeath = false),
                        ExitProcedure()
                    )
                }
                outOfTime && state.homeScore == state.awayScore -> {
                    compositeCommandOf(
                        ReportGoingIntoSuddenDeath(state),
                        GotoNode(SuddenDeath)
                    )
                }
                else -> {
                    GotoNode(Drive)
                }
            }
        }
    }

    object SuddenDeath : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure =
            com.jervisffb.engine.rules.bb2020.procedures.SuddenDeath
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                ReportGameResult(state, extraTime = true, suddenDeath = true),
                ExitProcedure()
            )
        }
    }
}
