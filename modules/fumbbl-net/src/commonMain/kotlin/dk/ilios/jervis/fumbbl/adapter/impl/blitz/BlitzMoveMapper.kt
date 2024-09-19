package dk.ilios.jervis.fumbbl.adapter.impl.blitz

import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerCoordinate
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.procedures.actions.move.StandardMoveStep

object BlitzMoveMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLITZ_MOVE
            && command.sound == "step"
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
        val moves: List<FieldModelSetPlayerCoordinate> = command.modelChangeList.filterIsInstance<FieldModelSetPlayerCoordinate>()
        moves.forEach {
            val coord = FieldCoordinate(it.value!!.x, it.value.y)
            newActions.add(MoveTypeSelected(MoveType.STANDARD), BlitzAction.MoveOrBlockOrEndAction)
            newActions.add(FieldSquareSelected(coord), StandardMoveStep.SelectTargetSquareOrEndAction)
        }
    }
}
