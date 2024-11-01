package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FumbblFoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerId
import com.jervisffb.fumbbl.net.utils.FumbblGame

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
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
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
                         action = EndAction, // (fumbblGame.actingPlayer.playerId!!.toJervisId()),
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
                        action = EndAction, // PlayerDeselected(fumbblGame.actingPlayer.playerId!!.toJervisId()),
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
