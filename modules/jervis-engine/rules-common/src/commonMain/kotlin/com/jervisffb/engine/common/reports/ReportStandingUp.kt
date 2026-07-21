package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.StandingUpRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportStandingUp(val context: StandingUpRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (context.isSuccess) {
            append("${context.player.name} successfully stood up")
        } else {
            append("${context.player.name} failed to stand up")
        }
    }
}
