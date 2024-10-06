package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.bb2020.tables.Weather

class ReportWeatherResult(weather: Weather) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (weather != Weather.PERFECT_CONDITIONS) {
            appendLine("Weather is ${weather.description}")
            append(weather.description)
        } else {
            append("Weather is ${weather.description}")
        }
    }
}
