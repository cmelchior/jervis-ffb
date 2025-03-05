package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerId
import com.jervisffb.fumbbl.net.utils.FumbblGame

/**
 * Active player ended its move action.
 *
 * Unclear why this is happening in two ways, probably I am missing something :/
 * I assume that player state is being used for a lot of other things as well.
 */
object EndMoveVariant2Mapper: CommandActionMapper {

    private fun reportNotHandled(cmd: ServerCommandModelSync) {
        println("Not handling: $cmd")
    }

    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE &&
            command.modelChangeList.size >= 3 && command.modelChangeList[2] is ActingPlayerSetPlayerId
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
        if ((command.modelChangeList[2] as ActingPlayerSetPlayerId).value == null) {
            when (fumbblGame.actingPlayer.playerAction) {
                PlayerAction.MOVE -> {
//                                if (jervisCommands.last().expectedNode == TeamTurn.DeselectPlayerOrSelectAction) {
//                                    jer
//                                } else {
                    newActions.add(EndAction, MoveAction.SelectMoveType)
//                                }
                }

                else -> reportNotHandled(command)
            }
        }
    }
}

