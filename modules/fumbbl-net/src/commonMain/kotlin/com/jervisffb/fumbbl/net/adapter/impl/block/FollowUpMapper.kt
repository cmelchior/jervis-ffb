package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.change.GameSetDialogParameter
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep

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
            if (previousCommand is GameSetDialogParameter && previousCommand.value?.dialogId == com.jervisffb.fumbbl.net.model.DialogId.FOLLOWUP_CHOICE) return true
            if (previousPreviousCommand is GameSetDialogParameter && previousPreviousCommand.value?.dialogId == com.jervisffb.fumbbl.net.model.DialogId.FOLLOWUP_CHOICE) return true
        }
        return false
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
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
            previousPreviousCommand.value?.dialogId == com.jervisffb.fumbbl.net.model.DialogId.FOLLOWUP_CHOICE
        ) {
            newActions.add(Confirm, PushStep.DecideToFollowUp)
        } else {
            newActions.add(Cancel, PushStep.DecideToFollowUp)
        }
    }
}
