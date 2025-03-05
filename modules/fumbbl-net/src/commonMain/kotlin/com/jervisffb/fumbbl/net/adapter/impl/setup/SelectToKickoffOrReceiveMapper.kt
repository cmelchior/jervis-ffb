package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.reports.ReceiveChoiceReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object SelectToKickoffOrReceiveMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // Handle selecting to receive or kick
        val report = command.reportList.firstOrNull() as ReceiveChoiceReport
        val receive = !report.receiveChoice
        newActions.add(if (receive) Cancel else Confirm, DetermineKickingTeam.ChooseKickingTeam)
    }
}
