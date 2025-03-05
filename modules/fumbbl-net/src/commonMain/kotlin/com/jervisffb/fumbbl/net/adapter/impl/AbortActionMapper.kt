package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.change.ActingPlayerSetPlayerId
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

/**
 * A user's action was aborted.
 *
 * This needs to be checked first...not 100% sure why.
 */
object AbortActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return false //
        // this check fails on Index 44 in 1624379
        // return command.reportList.size == 1 && command.reportList.first() is PlayerActionReport
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {

        // Abort a previous started action if possible (only move right now?).
        // Jervis doesn't support undoing actions right now, so just remove the first action from the action list.
        if (jervisCommands.last().expectedNode == ActivatePlayer.DeclareActionOrDeselectPlayer) {
            newActions.add(Undo, MoveAction.SelectMoveType) // Select Move Action
            newActions.add(Undo, ActivatePlayer.DeclareActionOrDeselectPlayer) // Select Player
        }

        when ((command.reportList.first() as PlayerActionReport).playerAction) {
            PlayerAction.MOVE -> {
                val movingPlayerId = command.modelChangeList.filterIsInstance<ActingPlayerSetPlayerId>().first().value!!
                val movingPlayer = jervisGame.getPlayerById(PlayerId(movingPlayerId.id))!!
                newActions.add(PlayerSelected(movingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
                newActions.add(
                    { state, rules -> PlayerActionSelected(rules.teamActions.move.type) },
                    ActivatePlayer.DeclareActionOrDeselectPlayer,
                )
            }
            else -> { /* Fall through */ }
        }
    }
}
