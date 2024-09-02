package dk.ilios.jervis.reports

import dk.ilios.jervis.rules.tables.Weather

class ReportWeatherResult(weather: Weather) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("Weather is ${weather.description}")
        append(weather.description)
    }
}
