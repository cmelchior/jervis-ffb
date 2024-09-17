package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.procedures.actions.move.MoveAction

/**
 * Active player ended its move action (variant 1)
 */
object EndPlayerTurn: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        val playerDeselected = command.modelChangeList.filterIsInstance<ActingPlayerSetPlayerId>().firstOrNull()?.let {
            it.value == null
        } ?: false
        return playerDeselected
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        when (val action = fumbblGame.actingPlayer.playerAction) {
            PlayerAction.MOVE -> newActions.add(EndAction, MoveAction.SelectMoveType)
            PlayerAction.BLOCK -> { /* Action ends automatically */ }
//            PlayerAction.BLITZ -> TODO()
            PlayerAction.BLITZ_MOVE -> newActions.add(EndAction, BlitzAction.RemainingMovesOrEndAction)
//            PlayerAction.BLITZ_SELECT -> TODO()
//            PlayerAction.HAND_OVER -> TODO()
//            PlayerAction.HAND_OVER_MOVE -> TODO()
//            PlayerAction.PASS -> TODO()
//            PlayerAction.PASS_MOVE -> TODO()
//            PlayerAction.FOUL -> TODO()
//            PlayerAction.FOUL_MOVE -> TODO()
//            PlayerAction.STAND_UP -> TODO()
//            PlayerAction.THROW_TEAM_MATE -> TODO()
//            PlayerAction.THROW_TEAM_MATE_MOVE -> TODO()
//            else -> TODO("Unsupported player action: $action.")
            else -> { /* Do nothing */ }
        }



    }
}
