package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.MortuaryAssistant

class ReportMortuaryAssistantUsed(team: Team, assistant: MortuaryAssistant) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} used a Mortuary Assistant"
}
