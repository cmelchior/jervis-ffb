package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetDrive
import dk.ilios.jervis.commands.SetHalf
import dk.ilios.jervis.commands.SetTurnMarker
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportGameResult
import dk.ilios.jervis.reports.ReportGoingIntoSuddenDeath
import dk.ilios.jervis.reports.ReportStartingDrive
import dk.ilios.jervis.reports.ReportStartingExtraTime
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration

/**
 * Procedure responsible for handling Extra Time as described on page 67 in the rulebook.
 */
object ExtraTime : Procedure() {
    override val initialNode: Node = DetermineKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
//        val currentHalf = state.halfNo + 1
//        // At start of game use the kicking team from the pre-game sequence, otherwise alternate teams based
//        // on who kicked off at last half.
//        var kickingTeam = state.kickingTeam
//        if (currentHalf > 1) {
//            kickingTeam = state.kickingTeamInLastHalf.otherTeam()
//        }
        return compositeCommandOf(
            SetHalf(state.halfNo + 1),
            SetDrive(0),
//            SetKickingTeamAtHalfTime(kickingTeam),
//            SetActiveTeam(kickingTeam.otherTeam()),
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = dk.ilios.jervis.procedures.DetermineKickingTeam
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = dk.ilios.jervis.procedures.SuddenDeath
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                ReportGameResult(state, extraTime = true, suddenDeath = true),
                ExitProcedure()
            )
        }
    }
}
