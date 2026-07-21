package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.rules.common.tables.Weather

class ReportWeatherResult(weather: Weather) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (weather != Weather.PERFECT_CONDITIONS) {
            appendLine("Weather is ${weather.title}")
            append(weather.description)
        } else {
            append("Weather is ${weather.description}")
        }
    }
}
