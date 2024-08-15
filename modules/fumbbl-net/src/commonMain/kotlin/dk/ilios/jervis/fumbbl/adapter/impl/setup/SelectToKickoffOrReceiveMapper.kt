package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.reports.ReceiveChoiceReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.DetermineKickingTeam

object SelectToKickoffOrReceiveMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.GAME_SET_DIALOG_PARAMETER &&
            command.reportList.firstOrNull() is ReceiveChoiceReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // Handle selecting to receive or kick
        val report = command.reportList.firstOrNull() as ReceiveChoiceReport
        val kicking = !report.receiveChoice
        newActions.add(if (kicking) Cancel else Confirm, DetermineKickingTeam.ChooseKickingTeam)
    }
}
