package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary

class ReportApothecaryUsed(team: Team, apothecary: Apothecary) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} used ${apothecary.type.description}"
}
