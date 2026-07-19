package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.block.ChompContext

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
