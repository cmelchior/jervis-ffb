package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.TurnMode
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerCoordinate
import com.jervisffb.fumbbl.net.model.change.FieldModelSetPlayerState
import com.jervisffb.fumbbl.net.utils.FumbblGame

// Moving a player for setting up a drive
// This is also being called when starting the half. Not sure why FUMBBL does this,
// but we just need to discard these events.
object SetupPlayerMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE &&
                command.modelChangeList.size == 2 &&
                command.reportList.isEmpty() &&
                command.modelChangeList[1].id == ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE &&
                game.turnMode == TurnMode.SETUP
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
        val playerId = (command.modelChangeList.first() as FieldModelSetPlayerState).key!!
        var coordinates = (command.modelChangeList[1] as FieldModelSetPlayerCoordinate).value!!
        val selectedPlayer = jervisGame.getPlayerById(PlayerId(playerId))!!
        newActions.add(PlayerSelected(selectedPlayer), SetupTeam.SelectPlayerOrEndSetup)
        if (coordinates.x < 0 || coordinates.y > 25) {
            newActions.add(DogoutSelected, SetupTeam.PlacePlayer)
        } else {
            newActions.add(FieldSquareSelected(coordinates.x, coordinates.y), SetupTeam.PlacePlayer)
        }
    }
}
