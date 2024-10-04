package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.ReportId
import dk.ilios.jervis.fumbbl.model.reports.InjuryReport
import dk.ilios.jervis.fumbbl.model.reports.TurnEndReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.TeamTurn

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
