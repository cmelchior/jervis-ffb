package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportFanFactor(team: Team, diceRoll: Int, dedicatedFans: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("${diceRoll + team.dedicatedFans}k total fans are cheering on ${team.name}")
        appendLine("${team.name} has ${team.dedicatedFans}k dedicated fans")
        append("${diceRoll}k fair-weather fans showed up")
    }
}
