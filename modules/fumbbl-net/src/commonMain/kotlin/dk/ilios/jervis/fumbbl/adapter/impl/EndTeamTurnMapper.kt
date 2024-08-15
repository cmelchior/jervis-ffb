package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.ReportId
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.TeamTurn

object EndTeamTurnMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED &&
            command.reportList.singleOrNull()?.reportId == ReportId.TURN_END
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        newActions.add(EndTurn, TeamTurn.SelectPlayerOrEndTurn)
    }
}
