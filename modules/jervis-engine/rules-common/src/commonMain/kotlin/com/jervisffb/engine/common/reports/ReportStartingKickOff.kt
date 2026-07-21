package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportStartingKickOff(kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${kickingTeam.name} is kicking off"
}
