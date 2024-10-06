package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.reports.WeatherReport
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.WeatherRoll

object WeatherRollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command .firstChangeId() == null &&
            command.firstReport() is WeatherReport
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
        val report = command.reportList.reports.first() as WeatherReport
        val weatherRoll = report.weatherRoll.map { D6Result(it) }
        newActions.add(DiceRollResults(weatherRoll), WeatherRoll.RollWeatherDice)
    }
}
