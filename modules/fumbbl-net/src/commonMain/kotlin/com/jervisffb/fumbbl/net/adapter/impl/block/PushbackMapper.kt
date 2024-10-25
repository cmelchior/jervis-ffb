package com.jervisffb.fumbbl.net.adapter.impl.blitz

import com.jervisffb.engine.actions.DirectionSelected
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

/**
 * Normal pushback
 */
object PushbackMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        // TODO Unclear why some pushbacks create a report while others do not
        val isUsingSideStep =  processedCommands.lastOrNull()?.firstReport()?.let {
            it is PushbackReport && it.pushbackMode == PushbackMode.SIDE_STEP
        } ?: false

        return (
            !isUsingSideStep &&
            command.modelChangeList.filterIsInstance<FieldModelRemovePushbackSquare>().count {
                it.value.selected
            } > 0 &&
            command.reportList.isEmpty()
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
        newActions.add(
            DirectionSelected(cmd.value.direction.transformToJervisDirection()),
            PushStep.SelectPushDirection
        )
    }
}
