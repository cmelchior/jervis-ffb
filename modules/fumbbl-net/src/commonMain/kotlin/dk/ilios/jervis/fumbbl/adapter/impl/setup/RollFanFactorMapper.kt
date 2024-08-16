package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.FanFactorReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.RollForStartingFanFactor

object RollFanFactorMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command.reportList.size == 2 &&
            command.reportList.reports[0] is FanFactorReport &&
            command.reportList.reports[1] is FanFactorReport
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
        // Start the game and roll for fan factor
        verifyReportSize(2, command)
        val homeTeamRoll = (command.reportList.reports[0] as FanFactorReport).dedicatedFansRoll
        val awayTeamRoll = (command.reportList.reports[1] as FanFactorReport).dedicatedFansRoll
        newActions.add(homeTeamRoll.d3, RollForStartingFanFactor.SetFanFactorForHomeTeam)
        newActions.add(awayTeamRoll.d3, RollForStartingFanFactor.SetFanFactorForAwayTeam)
    }

    private fun verifyReportSize(
        expectedSize: Int,
        command: ServerCommandModelSync,
    ) {
        if (command.reportList.reports.size != expectedSize) {
            throw IllegalStateException(
                "Expected reports of size $expectedSize, was ${command.reportList.reports.size}",
            )
        }
    }
}
