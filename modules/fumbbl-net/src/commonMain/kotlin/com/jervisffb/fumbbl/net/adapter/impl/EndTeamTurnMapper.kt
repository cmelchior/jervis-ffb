package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.TeamTurn
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.ReportId
import com.jervisffb.fumbbl.net.model.reports.InjuryReport
import com.jervisffb.fumbbl.net.model.reports.TurnEndReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object EndTeamTurnMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        val endOfTurnReport= (
            command.firstChangeId() == ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED &&
            command.reportList.singleOrNull()?.reportId == ReportId.TURN_END
        )
        if (endOfTurnReport) {
            if (processedCommands.size >= 7) {
                val cmd = processedCommands[processedCommands.size - 7]
                val firstReport = cmd.reportList.firstOrNull()
                if (firstReport is InjuryReport) {
                    // Player on active team was Injured during a Block = TurnOver
                    val homeTurnover = game.homePlaying && game.teamHome.players.any { it.playerId == firstReport.defenderId.id }
                    val awayTurnover = !game.homePlaying && game.teamAway.players.any { it.playerId == firstReport.defenderId.id }
                    if (homeTurnover || awayTurnover) {
                        return false
                    }
                }
            }
            return true
        }
        return false
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // TODO This doesn't detect turn overs correctly. We should only
        //  manually send this when the player selected "EndTurn"
        val report = command.firstReport() as TurnEndReport
        // Touchdowns trigger a turn over
        val isTouchdown = (report.playerIdTouchdown != null)

        if (!isTouchdown) {
            newActions.add(EndTurn, TeamTurn.SelectPlayerOrEndTurn)
        }
    }
}
