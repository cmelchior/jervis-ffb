package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.change.GameSetSetupOffense
import com.jervisffb.fumbbl.net.utils.FumbblGame

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
