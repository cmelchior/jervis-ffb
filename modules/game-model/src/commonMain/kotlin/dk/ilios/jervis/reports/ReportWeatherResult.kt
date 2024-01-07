package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.rules.tables.Weather

class ReportWeatherResult(firstD6: D6Result, secondD6: D6Result, weather: Weather) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Weather rolled [${firstD6.result}, ${secondD6.result}]. Weather is $weather."
}
