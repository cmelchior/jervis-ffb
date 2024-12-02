package com.jervisffb.fumbbl.net.adapter.impl.move

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object StartMoveActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is PlayerActionReport &&
            firstReport.playerAction == PlayerAction.MOVE
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        val report = command.firstReport() as PlayerActionReport
        val movingPlayer = jervisGame.getPlayerById(PlayerId(report.actingPlayerId.id))
        newActions.add(PlayerSelected(movingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            { state, rules -> PlayerActionSelected(rules.teamActions.move.type) },
            ActivatePlayer.DeclareActionOrDeselectPlayer
        )
    }
}
