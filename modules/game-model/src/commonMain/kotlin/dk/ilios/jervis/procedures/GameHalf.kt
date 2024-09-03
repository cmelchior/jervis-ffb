package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetAvailableTeamRerolls
import dk.ilios.jervis.commands.SetDrive
import dk.ilios.jervis.commands.SetHalf
import dk.ilios.jervis.commands.SetKickingTeamAtHalfTime
import dk.ilios.jervis.commands.SetTurnNo
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportStartingDrive
import dk.ilios.jervis.reports.ReportStartingHalf
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.ResetPolicy

object GameHalf : Procedure() {
    override val initialNode: Node = Drive
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val currentHalf = state.halfNo + 1
        // At start of game use the kicking team from the pre-game sequence, otherwise alternate teams based
        // on who kicked off at last half.
        var kickingTeam = state.kickingTeam
        if (currentHalf > 1) {
            kickingTeam = state.kickingTeamInLastHalf.otherTeam()
        }
        return compositeCommandOf(
            SetHalf(currentHalf),
            SetDrive(0),
            SetKickingTeamAtHalfTime(kickingTeam),
            SetActiveTeam(kickingTeam.otherTeam()),
            SetAvailableTeamRerolls(state.homeTeam),
            SetAvailableTeamRerolls(state.awayTeam),
            SetTurnNo(state.homeTeam, 0),
            SetTurnNo(state.awayTeam, 0),
            ReportStartingHalf(currentHalf),
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        // Remove modifiers that only last this half
        val resetCommands = getResetTemporaryModifiersCommands(state, rules, ResetPolicy.END_OF_HALF)
        return compositeCommandOf(
            *resetCommands
        )
    }

    object Drive : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameDrive
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
                ReportStartingDrive(drive),
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // Both teams ran out of time
            return if (state.homeTeam.turnData.currentTurn == rules.turnsPrHalf && state.awayTeam.turnData.currentTurn == rules.turnsPrHalf) {
                ExitProcedure()
            } else {
                GotoNode(Drive)
            }
        }
    }
}
