package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.bb2020.procedures.actions.move.StandingUpRollContext

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
