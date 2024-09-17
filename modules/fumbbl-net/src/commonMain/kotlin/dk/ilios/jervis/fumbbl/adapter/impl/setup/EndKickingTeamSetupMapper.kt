package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.change.GameSetSetupOffense
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.SetupTeam

object EndKickingTeamSetupMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.GAME_SET_HOME_PLAYING &&
//            game.turnMode == TurnMode.SETUP &&
//            command.modelChangeList.size == 3 &&
//            command.modelChangeList.last() is GameSetTurnMode
            (command.modelChangeList.lastOrNull()?.let { it is GameSetSetupOffense && it.value } ?: false)
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
        // Ending second team setup
        newActions.add(EndSetup, SetupTeam.SelectPlayerOrEndSetup)
    }
}
