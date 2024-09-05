package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetKickingTeam
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.ReportSetupKickingTeam
import dk.ilios.jervis.reports.ReportSetupReceivingTeam
import dk.ilios.jervis.reports.ReportStartingKickOff
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

object GameDrive : Procedure() {
    override val initialNode: Node = SetupKickingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SetupKickingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam

        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.kickingTeam),
                ReportSetupKickingTeam(state.kickingTeam),
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SetupReceivingTeam)
        }
    }

    object SetupReceivingTeam : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = SetupTeam

        override fun onEnterNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.receivingTeam),
                ReportSetupReceivingTeam(state.receivingTeam),
            )
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(KickOff)
        }
    }

    object KickOff : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOff
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return ReportStartingKickOff(state.kickingTeam)
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                GotoNode(KickOffEvent),
            )
        }
    }

    object KickOffEvent : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TheKickOffEvent
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetActiveTeam(state.receivingTeam),
                GotoNode(Turn),
            )
        }
    }

    object Turn : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = TeamTurn
        override fun onExitNode(state: Game, rules: Rules): Command {
            // TODO This logic is completely messed up. A Drive also ends at the end of the half
            val switchTeamCommands =
                compositeCommandOf(
                    SetActiveTeam(state.inactiveTeam),
                    SetKickingTeam(state.receivingTeam),
                )
            val goalScored = state.goalScored || state.isTurnOver
            return if (goalScored) {
                // TODO this is probably wrong if the inactive team scored. I.e. at the end of the half
                compositeCommandOf(
                    switchTeamCommands,
                    ExitProcedure()
                )
            } else if (state.homeTeam.turnData.turnMarker == rules.turnsPrHalf && state.awayTeam.turnData.turnMarker == rules.turnsPrHalf) {
                GotoNode(ResolveEndOfDrive)
                // The other team can continue the drive
            } else if (state.inactiveTeam.turnData.turnMarker < rules.turnsPrHalf) {
                switchTeamCommands + GotoNode(Turn)
            } else {
                INVALID_GAME_STATE()
            }
        }
    }

    object ResolveEndOfDrive : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = EndOfDriveSequence
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The End of Drive Sequence doesn't mention moving players off the pitch, so we
            // do it here after the sequence has completed.
            val movePlayers: List<SetPlayerLocation> =
                state.field
                    .filter { !it.isUnoccupied() }
                    .map {
                        SetPlayerLocation(it.player!!, DogOut)
                    }
            return compositeCommandOf(
                *movePlayers.toTypedArray(),
                ExitProcedure(),
            )
        }
    }
}
