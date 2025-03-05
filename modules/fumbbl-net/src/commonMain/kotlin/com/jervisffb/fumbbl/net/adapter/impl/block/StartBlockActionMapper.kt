package com.jervisffb.fumbbl.net.adapter.impl.block

import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayer
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.reports.PlayerActionReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object StartBlockActionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val firstReport = command.firstReport()
        return (
            firstReport is PlayerActionReport &&
                firstReport.playerAction == PlayerAction.BLOCK
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
        val blockingPlayer = jervisGame.getPlayerById(report.actingPlayerId.toJervisId())
        newActions.add(PlayerSelected(blockingPlayer.id), TeamTurn.SelectPlayerOrEndTurn)
        newActions.add(
            action = { _: Game, rules -> PlayerActionSelected(rules.teamActions.block.type) },
            expectedNode = ActivatePlayer.DeclareActionOrDeselectPlayer
        )
    }
}
