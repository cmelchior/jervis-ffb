package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.MortuaryAssistant
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportMortuaryAssistantUsed(team: Team, assistant: MortuaryAssistant) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} used a Mortuary Assistant"
}
