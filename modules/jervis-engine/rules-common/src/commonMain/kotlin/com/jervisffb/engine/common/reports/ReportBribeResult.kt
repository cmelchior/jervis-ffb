package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportBribeResult(team: Team, success: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = when (success) {
        true -> "${team.name} successfully bribed the referee"
        false -> "${team.name} failed to bribe the referee"
    }
}
