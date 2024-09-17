package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.move.MoveAction

/**
 * Active player ended its move action (variant 3)
 */
object EndMoveVariant3Mapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val setActivePlayer = command.modelChangeList.filterIsInstance<ActingPlayerSetPlayerId>().firstOrNull()
        // Active player is removed = Action ended
        return (
            setActivePlayer != null &&
            setActivePlayer.value == null &&
            game.actingPlayer.playerAction == PlayerAction.MOVE
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
        newActions.add(EndAction, MoveAction.SelectMoveType)
    }
}
