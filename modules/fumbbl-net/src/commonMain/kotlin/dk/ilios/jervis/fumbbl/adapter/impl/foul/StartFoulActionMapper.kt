package dk.ilios.jervis.fumbbl.adapter.impl.foul

import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.ActivatePlayer
import dk.ilios.jervis.procedures.TeamTurn

object StartFoulActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is PlayerActionReport &&
                firstReport.playerAction == PlayerAction.FOUL_MOVE
            )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.reportList.first() as PlayerActionReport
        newActions.add(PlayerSelected(report.actingPlayerId.toJervisId()), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            { state, rules -> PlayerActionSelected(rules.teamActions.foul.type) },
            ActivatePlayer.DeclareActionOrDeselectPlayer
        )

        // There is a bug in FUMBBL, so you do not have to select the Foul target
        // when starting the action. I.e. you decide later who to foul.
        // The only way to get around this is by going forward in the logs in



//
//        val startActionCommand = processedCommands[processedCommands.size - 2]
//        if (startActionCommand.firstReport() !is PlayerActionReport) {
//            throw IllegalStateException("Unexpected state: ${startActionCommand.firstReport()}")
//        }
//        val playerStandingUp = startActionCommand.modelChangeList
//            .filterIsInstance<ActingPlayerSetStandingUp>()
//            .count { it.value } > 0
//        if (playerStandingUp) {
//            newActions.add(MoveTypeSelected(MoveType.STAND_UP), BlitzAction.MoveOrBlockOrEndAction)
//        }
    }
}
