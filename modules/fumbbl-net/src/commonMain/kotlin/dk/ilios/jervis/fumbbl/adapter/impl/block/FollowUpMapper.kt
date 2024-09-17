package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.DialogId
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.change.GameSetDialogParameter
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.PushStep

/**
 * Looks like this choice is hidden between a lot of other stuff.
 * Unclear if there is a better way to detect it
 */
object FollowUpMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val isShowingOrHidingDialog = command.firstChangeId() == ModelChangeId.GAME_SET_DIALOG_PARAMETER
        if (isShowingOrHidingDialog) {
            // Check last two commands, if any of them is showing a followup dialog, it means a choice was made
            val previousCommand = processedCommands.last().modelChangeList.firstOrNull()
            val previousPreviousCommand = processedCommands[processedCommands.size - 2].modelChangeList.firstOrNull()
            if (previousCommand is GameSetDialogParameter && previousCommand.value?.dialogId == DialogId.FOLLOWUP_CHOICE) return true
            if (previousPreviousCommand is GameSetDialogParameter && previousPreviousCommand.value?.dialogId == DialogId.FOLLOWUP_CHOICE) return true
        }
        return false
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // If the dialog was shown 2 commands ago, it means that command - 1 was moving the player because
        // they accepted to follow up.
        val previousPreviousCommand = processedCommands[processedCommands.size - 2].modelChangeList.firstOrNull()

        if (
            (previousPreviousCommand is GameSetDialogParameter) &&
            previousPreviousCommand.value?.dialogId == DialogId.FOLLOWUP_CHOICE
        ) {
            newActions.add(Confirm, PushStep.DecideToFollowUp)
        } else {
            newActions.add(Cancel, PushStep.DecideToFollowUp)
        }
    }
}
