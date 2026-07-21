package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.procedures.actions.block.ChompContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportChompResult(context: ChompContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (context.isSuccess) {
            append("${context.attacker.name} chomped down on ${context.defender!!.name}.")
        } else {
            append("${context.attacker.name} failed to chomped down on ${context.defender!!.name}.")
        }
    }
}
