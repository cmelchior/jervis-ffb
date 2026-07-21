package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportStartingTurn(team: Team, turn: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting turn for ${team.name}: $turn"
}
