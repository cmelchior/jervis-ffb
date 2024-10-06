package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportSetupKickingTeam(kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${kickingTeam.name} is setting up for a kick."
}
