package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team

class ReportSetupReceivingTeam(receivingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${receivingTeam.name} is setting up to receive."
}
