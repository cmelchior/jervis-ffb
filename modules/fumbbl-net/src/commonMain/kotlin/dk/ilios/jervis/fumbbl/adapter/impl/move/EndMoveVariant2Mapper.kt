package dk.ilios.jervis.fumbbl.adapter.impl.move

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.MoveAction

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
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE &&
                command.modelChangeList.size >= 3 && command.modelChangeList[2] is ActingPlayerSetPlayerId
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
        if ((command.modelChangeList[2] as ActingPlayerSetPlayerId).value == null) {
            when (fumbblGame.actingPlayer.playerAction) {
                PlayerAction.MOVE -> {
//                                if (jervisCommands.last().expectedNode == TeamTurn.DeselectPlayerOrSelectAction) {
//                                    jer
//                                } else {
                    newActions.add(EndAction, MoveAction.SelectSquareOrEndAction)
//                                }
                }

                else -> reportNotHandled(command)
            }
        }
    }
}

