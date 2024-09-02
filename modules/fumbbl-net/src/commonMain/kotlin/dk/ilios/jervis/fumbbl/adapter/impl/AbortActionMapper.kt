package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.Undo
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.TeamTurn
import dk.ilios.jervis.procedures.actions.move.MoveAction

/**
 * A user's action was aborted.
 *
 * This needs to be checked first...not 100% sure why.
 */
object AbortActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return false //
        // this check fails on Index 44 in 1624379
        // return command.reportList.size == 1 && command.reportList.first() is PlayerActionReport
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {

        // Abort a previous started action if possible (only move right now?).
        // Jervis doesn't support undoing actions right now, so just remove the first action from the action list.
        if (jervisCommands.last().expectedNode == TeamTurn.DeselectPlayerOrSelectAction) {
            newActions.add(Undo, MoveAction.SelectMoveType) // Select Move Action
            newActions.add(Undo, TeamTurn.DeselectPlayerOrSelectAction) // Select Player
        }

        when ((command.reportList.first() as PlayerActionReport).playerAction) {
            PlayerAction.MOVE -> {
                val movingPlayerId = command.modelChangeList.filterIsInstance<ActingPlayerSetPlayerId>().first().value!!
                val movingPlayer = jervisGame.getPlayerById(PlayerId(movingPlayerId.id))!!
                newActions.add(PlayerSelected(movingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
                newActions.add(
                    { state, rules -> PlayerActionSelected(rules.teamActions.move.action.type) },
                    TeamTurn.DeselectPlayerOrSelectAction,
                )
            }
            else -> { /* Fall through */ }
        }
    }
}
