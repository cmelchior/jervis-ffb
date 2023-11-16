package dk.ilios.bowlbot.procedures

import compositeCommandOf
import dk.ilios.bowlbot.commands.Command
import dk.ilios.bowlbot.commands.ExitProcedure
import dk.ilios.bowlbot.commands.GotoNode
import dk.ilios.bowlbot.commands.SetActiveTeam
import dk.ilios.bowlbot.commands.SetKickingTeam
import dk.ilios.bowlbot.commands.SetTurnNo
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.ReportEndingTurn
import dk.ilios.bowlbot.logs.ReportSetupKickingTeam
import dk.ilios.bowlbot.logs.ReportSetupReceivingTeam
import dk.ilios.bowlbot.logs.ReportStartingKickOff
import dk.ilios.bowlbot.logs.ReportStartingTurn
import dk.ilios.bowlbot.model.Game
import dk.ilios.bowlbot.rules.Rules

object GameDrive: Procedure {
    override val initialNode: Node = SetupKickingTeam

    object SetupKickingTeam: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onEnter(state: Game, rules: Rules): Command {
            return ReportSetupKickingTeam(state.kickingTeam)
        }
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(SetupReceivingTeam)
        }
    }

    object SetupReceivingTeam: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onEnter(state: Game, rules: Rules): Command {
            return ReportSetupReceivingTeam(state.receivingTeam)
        }
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(KickOff)
        }
    }

    object KickOff: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onEnter(state: Game, rules: Rules): Command {
            return ReportStartingKickOff(state.kickingTeam)
        }
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(KickOffEvent)
        }
    }

    object KickOffEvent: ParentNode() {
        override val childProcedure: Procedure = DummyProcedure
        override fun onExit(state: Game, rules: Rules): Command {
            return GotoNode(Turn)
        }
    }

    object Turn: ParentNode() {
        override val childProcedure = GameTurn

        override fun onEnter(state: Game, rules: Rules): Command {
            val turn = state.activeTeam.turnData.currentTurn + 1
            return compositeCommandOf(
                SetTurnNo(state.activeTeam, turn),
                ReportStartingTurn(state.activeTeam, turn)
            )
        }
        override fun onExit(state: Game, rules: Rules): Command {
            val goalScored = state.goalScored
            val sharedCommands = compositeCommandOf(
                ReportEndingTurn(state.activeTeam, state.activeTeam.turnData.currentTurn),
                SetActiveTeam(state.inactiveTeam),
                SetKickingTeam(state.receivingTeam)
            )

            // Goal has been scored
            if (goalScored) {
                return sharedCommands + ExitProcedure()
            // Both teams ran out of time
            } else if (state.homeTeam.turnData.currentTurn == rules.turnsPrHalf && state.awayTeam.turnData.currentTurn == rules.turnsPrHalf) {
                return sharedCommands + ExitProcedure()
            // The other team can continue the drive
            } else if (state.inactiveTeam.turnData.currentTurn < rules.turnsPrHalf) {
                return sharedCommands + GotoNode(Turn)
            } else {
                TODO()
            }
        }
    }
}