package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.reports.WeatherReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.WeatherRoll

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
