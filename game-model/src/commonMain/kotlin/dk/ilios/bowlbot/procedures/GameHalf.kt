package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.ReportLog
import dk.ilios.bowlbot.commands.SetActiveTeam
import dk.ilios.bowlbot.commands.SetDrive
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportStartingDrive
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object GameHalf: Procedure {
    override val initialNode: Node = Drive
    object Drive: ParentNode() {
        override val childProcedure: Procedure = GameDrive
        override fun onEnter(state: Game, rules: Rules): Command {
            val drive: Int = state.driveNo + 1
            return compositeCommandOf(
                SetDrive(drive),
//                ResetTeamDriveData(state.homeTeam),
//                ResetTeamDriveData(state.awayTeam)
                ReportLog(ReportStartingDrive(drive))
            )
        }

        override fun onExit(state: Game, rules: Rules): Command {
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
}