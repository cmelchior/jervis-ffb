package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.SetActiveTeam
import dk.ilios.bowlbot.commands.SetDrive
import dk.ilios.bowlbot.commands.SetHalf
import dk.ilios.bowlbot.commands.SetKickingTeamAtHalfTime
import dk.ilios.bowlbot.commands.SetTurnNo
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportStartingDrive
import dk.ilios.bowlbot.logs.ReportStartingHalf
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object GameHalf: Procedure() {
    override val initialNode: Node = Drive

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
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
            SetTurnNo(state.homeTeam, 0),
            SetTurnNo(state.awayTeam, 0),
            ReportStartingHalf(currentHalf)
        )
    }

    object Drive: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameDrive
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
                ReportStartingDrive(drive)
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // Both teams ran out of time
            if (state.homeTeam.turnData.currentTurn == rules.turnsPrHalf && state.awayTeam.turnData.currentTurn == rules.turnsPrHalf) {
                return compositeCommandOf(
                    ExitProcedure()
                )
            } else {
                return GotoNode(Drive)
            }
        }
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
}