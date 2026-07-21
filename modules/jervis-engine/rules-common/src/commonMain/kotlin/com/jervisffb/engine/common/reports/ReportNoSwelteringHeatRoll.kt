package com.jervisffb.engine.rules.common.procedures.tables.weather

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportNoSwelteringHeatRoll(team: Team): LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} does not have any players eligible for a Sweltering Heat roll."
}
