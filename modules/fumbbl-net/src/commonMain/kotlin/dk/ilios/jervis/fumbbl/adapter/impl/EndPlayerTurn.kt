package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.procedures.actions.block.BlockAction
import dk.ilios.jervis.procedures.actions.foul.FumbblFoulAction
import dk.ilios.jervis.procedures.actions.move.MoveAction

/**
 * Active player ended its action (variant 1)
 */
object EndPlayerTurn: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        when (val action = fumbblGame.actingPlayer.playerAction) {
            PlayerAction.MOVE -> newActions.add(EndAction, MoveAction.SelectMoveType)
            PlayerAction.BLOCK -> {
                // If the player hasn't blocked, it means they stopped the block early. So it needs
                // to be manually canceled.
                if (!fumbblGame.actingPlayer.hasBlocked) {
                     newActions.add(
                         action = PlayerDeselected(fumbblGame.actingPlayer.playerId!!.toJervisId()),
                         expectedNode = BlockAction.SelectDefenderOrEndAction
                     )
                }
            }
//            PlayerAction.BLITZ -> TODO()
            PlayerAction.BLITZ_MOVE -> newActions.add(EndAction, BlitzAction.RemainingMovesOrEndAction)
//            PlayerAction.BLITZ_SELECT -> TODO()
//            PlayerAction.HAND_OVER -> TODO()
//            PlayerAction.HAND_OVER_MOVE -> TODO()
//            PlayerAction.PASS -> TODO()
//            PlayerAction.PASS_MOVE -> TODO()
//            PlayerAction.FOUL -> TODO()
            PlayerAction.FOUL_MOVE ->  {
                // If the player hasn't fouled or moved, it means they stopped the block early. So it needs
                // to be manually canceled.
                if (!fumbblGame.actingPlayer.hasFouled && !fumbblGame.actingPlayer.hasMoved) {
                    newActions.add(
                        action = PlayerDeselected(fumbblGame.actingPlayer.playerId!!.toJervisId()),
                        expectedNode = FumbblFoulAction.MoveOrFoulOrEndAction
                    )
                }
            }
//            PlayerAction.STAND_UP -> TODO()
//            PlayerAction.THROW_TEAM_MATE -> TODO()
//            PlayerAction.THROW_TEAM_MATE_MOVE -> TODO()
//            else -> TODO("Unsupported player action: $action.")
            else -> { /* Do nothing */ }
        }



    }
}
