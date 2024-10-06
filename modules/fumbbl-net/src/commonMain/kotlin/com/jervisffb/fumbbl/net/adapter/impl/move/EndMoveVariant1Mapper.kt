package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction

/**
 * Active player ended its move action (variant 1)
 */
object EndMoveVariant1Mapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        // This is not right. Figure out a way to better handle how the Client undo actions
        return false //
        return command.firstChangeId() == ModelChangeId.ACTING_PLAYER_SET_HAS_MOVED
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
