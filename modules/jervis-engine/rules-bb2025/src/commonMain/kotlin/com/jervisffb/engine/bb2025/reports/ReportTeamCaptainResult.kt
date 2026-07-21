package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.procedures.rerolls.TeamCaptainRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.rules.common.skills.RerollSource

class ReportTeamCaptainResult(context: TeamCaptainRollContext, reroll: RerollSource) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.player.name} made ${reroll.rerollDescription} a free re-roll")
    }
}
