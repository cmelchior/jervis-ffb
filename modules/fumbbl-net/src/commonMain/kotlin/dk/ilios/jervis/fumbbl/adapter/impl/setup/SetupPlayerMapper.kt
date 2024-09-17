package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.TurnMode
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerCoordinate
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerState
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.SetupTeam

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
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
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
