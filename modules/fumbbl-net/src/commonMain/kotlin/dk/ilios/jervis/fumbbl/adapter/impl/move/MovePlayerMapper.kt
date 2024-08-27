package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerCoordinate
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.move.MoveAction

object MovePlayerMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        //  command.firstChangeId() == ModelChangeId.ACTING_PLAYER_SET_CURRENT_MOVE
        return (
            game.actingPlayer.playerAction == PlayerAction.MOVE
            && command.sound == "step"
        )
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val moves: List<FieldModelSetPlayerCoordinate> = command.modelChangeList.filterIsInstance<FieldModelSetPlayerCoordinate>()
        moves.forEach {
            val coord = FieldCoordinate(it.value!!.x, it.value.y)
            newActions.add(FieldSquareSelected(coord), MoveAction.SelectMoveType)
        }
    }
}
