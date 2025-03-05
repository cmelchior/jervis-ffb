package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.PushStep
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PushbackMode
import com.jervisffb.fumbbl.net.model.change.FieldModelRemovePushbackSquare
import com.jervisffb.fumbbl.net.model.reports.PushbackReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object PushbackUsingSideStepMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            processedCommands.last().firstReport()?.let {
                it is PushbackReport && it.pushbackMode == PushbackMode.SIDE_STEP
            } ?: false
        )
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val cmd = command.modelChangeList.first {
            it is FieldModelRemovePushbackSquare && it.value.selected
        } as FieldModelRemovePushbackSquare
        val target = cmd.value.coordinate

        newActions.add(Confirm, PushStep.DecideToUseSidestep)
        newActions.add(FieldSquareSelected(target.x, target.y), PushStep.SelectPushDirection)
    }
}
