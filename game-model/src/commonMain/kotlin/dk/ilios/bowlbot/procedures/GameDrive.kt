package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.SetActiveTeam
import dk.ilios.bowlbot.commands.SetTurn
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportEndingTurn
import dk.ilios.bowlbot.logs.ReportStartingTurn
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object GameDrive: Procedure {
    override val initialNode: Node = Turn

    object Turn: ParentNode() {
        override val childProcedure = DummyProcedure
        override fun onEnter(state: Game, rules: Rules): Command {
            val turn = state.currentTeam.turnData.currentTurn + 1
            return compositeCommandOf(
                SetTurn(state.currentTeam, turn),
                ReportStartingTurn(state.currentTeam, turn)
            )
        }
        override fun onExit(state: Game, rules: Rules): Command {

            val goalScored = state.goalScored

            val sharedCommands = compositeCommandOf(
                ReportEndingTurn(state.currentTeam, state.currentTeam.turnData.currentTurn),
                SetActiveTeam(state.otherTeam),
            )

            // Goal has been scored
            if (goalScored) {
                return sharedCommands + ExitProcedure()
            // Both teams ran out of time
            } else if (state.homeTeam.turnData.currentTurn == rules.turnsPrHalf && state.awayTeam.turnData.currentTurn == rules.turnsPrHalf) {
                return sharedCommands + ExitProcedure()
            // The other team can continue the drive
            } else if (state.otherTeam.turnData.currentTurn < rules.turnsPrHalf) {
                return sharedCommands + GotoNode(Turn)
            } else {
                TODO()
            }
        }
    }
}