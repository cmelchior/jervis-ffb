package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.ext.d3
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.model.reports.FanFactorReport
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.FanFactorRolls

object RollFanFactorMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // Start the game and roll for fan factor
        verifyReportSize(2, command)
        val homeTeamRoll = (command.reportList.reports[0] as FanFactorReport).dedicatedFansRoll
        val awayTeamRoll = (command.reportList.reports[1] as FanFactorReport).dedicatedFansRoll
        newActions.add(homeTeamRoll.d3, FanFactorRolls.SetFanFactorForHomeTeam)
        newActions.add(awayTeamRoll.d3, FanFactorRolls.SetFanFactorForAwayTeam)
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
