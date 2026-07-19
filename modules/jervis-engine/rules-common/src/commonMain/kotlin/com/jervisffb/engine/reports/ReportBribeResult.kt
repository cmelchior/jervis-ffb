package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportBribeResult(team: Team, success: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = when (success) {
        true -> "${team.name} successfully bribed the referee"
        false -> "${team.name} failed to bribe the referee"
    }
}
