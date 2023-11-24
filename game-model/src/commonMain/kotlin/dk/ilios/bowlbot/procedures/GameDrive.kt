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
import dk.ilios.bowlbot.utils.INVALID_GAME_STATE

object GameDrive: Procedure() {
    override val initialNode: Node = SetupKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetupKickingTeam: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return ReportSetupKickingTeam(state.kickingTeam)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SetupReceivingTeam)
        }
    }

    object SetupReceivingTeam: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return ReportSetupReceivingTeam(state.receivingTeam)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOff)
        }
    }

    object KickOff: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return ReportStartingKickOff(state.kickingTeam)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOffEvent)
        }
    }

    object KickOffEvent: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(Turn)
        }
    }

    object Turn: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = GameTurn
        override fun onExitNode(state: Game, rules: Rules): Command {
            val switchTeamCommands = compositeCommandOf(
                SetActiveTeam(state.inactiveTeam),
                SetKickingTeam(state.receivingTeam)
            )
            val goalScored = state.goalScored
            if (goalScored) {
                // TODO this is probably wrong if the inactive team scored. I.e. at the end of the half
                return switchTeamCommands + ExitProcedure()
            } else if (state.homeTeam.turnData.currentTurn == rules.turnsPrHalf && state.awayTeam.turnData.currentTurn == rules.turnsPrHalf) {
                return ExitProcedure()
            // The other team can continue the drive
            } else if (state.inactiveTeam.turnData.currentTurn < rules.turnsPrHalf) {
                return switchTeamCommands + GotoNode(Turn)
            } else {
                INVALID_GAME_STATE()
            }
        }
    }
}