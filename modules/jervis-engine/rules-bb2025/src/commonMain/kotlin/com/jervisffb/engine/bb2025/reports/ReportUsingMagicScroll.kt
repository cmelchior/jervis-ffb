package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportUsingMagicScroll(team: Team): LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} used a Magic Scroll and gained a Sports-Wizard"
}
