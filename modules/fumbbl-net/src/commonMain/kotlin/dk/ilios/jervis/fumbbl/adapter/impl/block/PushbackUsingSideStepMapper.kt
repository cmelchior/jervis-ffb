package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PushbackMode
import dk.ilios.jervis.fumbbl.model.change.FieldModelRemovePushbackSquare
import dk.ilios.jervis.fumbbl.model.reports.PushbackReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.PushStep

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
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
