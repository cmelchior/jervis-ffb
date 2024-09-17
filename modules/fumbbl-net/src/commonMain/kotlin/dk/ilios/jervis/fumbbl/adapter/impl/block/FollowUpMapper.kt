package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game

/**
 * Looks like this choice is hidden between a lot of other stuff.
 * Unclear if there is a better way to detect it
 */
object FollowUpMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return false
//        // For now, check if the last command was something that could turn this into a followup
//        return  (
//            processedCommands.lastOrNull()?.firstReport() is PushbackReport &&
//            command.modelChangeList.filterIsInstance<FieldModelSetPlayerCoordinate>().isNotEmpty()
//        )
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
//
//        val sidestepCommand = processedCommands.last().modelChangeList.first {
//            it is FieldModelAddPushbackSquare && it.value.selected
//        } as FieldModelAddPushbackSquare
//        val sidestepTo = sidestepCommand.value.coordinate
//
//        val followUp = command.modelChangeList.firstOrNull {
//            it is FieldModelSetPlayerCoordinate && it.value == sidestepTo
//        } != null
//
//        // How to detect
//        newActions.add(if (followUp) Confirm else Cancel, PushStep.DecideToFollowUp)
    }
}
