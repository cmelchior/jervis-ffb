package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerId
import com.jervisffb.fumbbl.net.utils.FumbblGame

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
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        newActions.add(EndAction, MoveAction.SelectMoveType)
    }
}
