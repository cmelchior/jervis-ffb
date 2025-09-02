package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.SecureTheBallRollContext

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
