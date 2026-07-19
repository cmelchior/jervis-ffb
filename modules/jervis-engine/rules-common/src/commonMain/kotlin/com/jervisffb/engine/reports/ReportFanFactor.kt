package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportFanFactor(team: Team, fairWeatherFans: Int, dedicatedFans: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("${fairWeatherFans}k fair-weather fans showed up")
        appendLine("${team.name} has ${dedicatedFans}k dedicated fans")
        append("${fairWeatherFans + dedicatedFans}k total fans are cheering on ${team.name}")
    }
}
