package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.context.SecureTheBallRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportSecuredTheBallResult(val context: SecureTheBallRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (context.isSuccess) {
            append("${context.player.name} secured the ball")
        } else {
            append("${context.player.name} failed to secured the ball")
        }
    }
}
