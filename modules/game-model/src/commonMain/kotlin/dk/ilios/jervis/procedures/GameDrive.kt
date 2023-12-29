package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.ReportSetupKickingTeam
import dk.ilios.jervis.reports.ReportSetupReceivingTeam
import dk.ilios.jervis.reports.ReportStartingKickOff
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

object GameDrive: Procedure() {
    override val initialNode: Node = SetupKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetupKickingTeam: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.kickingTeam),
                ReportSetupKickingTeam(state.kickingTeam)
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SetupReceivingTeam)
        }
    }

    object SetupReceivingTeam: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.receivingTeam),
                ReportSetupReceivingTeam(state.receivingTeam)
            )
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOff)
        }
    }

    object KickOff: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOff
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return ReportStartingKickOff(state.kickingTeam)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                GotoNode(KickOffEvent)
            )
        }
    }

    object KickOffEvent: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOffEvent
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.receivingTeam),
                GotoNode(Turn)
            )
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
                return GotoNode(EndDrive)
            // The other team can continue the drive
            } else if (state.inactiveTeam.turnData.currentTurn < rules.turnsPrHalf) {
                return switchTeamCommands + GotoNode(Turn)
            } else {
                INVALID_GAME_STATE()
            }
        }
    }

    object EndDrive: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Move all players on the field back to reserves
            // TODO Roll for secret weapons
            // TODO Other stuff?
            val movePlayers: List<SetPlayerLocation> = state.field
                .filter { !it.isEmpty() }
                .map { SetPlayerLocation(it.player!!, DogOut)
            }
            return compositeCommandOf(
                *movePlayers.toTypedArray(),
                ExitProcedure()
            )
        }
    }
}