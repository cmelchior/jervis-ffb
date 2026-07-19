package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportStartingCharge(team: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting Charge! for ${team.name}"
}
