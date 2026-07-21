package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.procedures.actions.block.DauntlessRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportDauntlessResult(context: DauntlessRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (context.isSuccess) {
            val modifier = context.modifier!!.modifier
            append("${context.attacker.name} increases their Strength by +$modifier")
        } else {
            append("Dauntless fails to increase the strength of ${context.attacker.name}")
        }
    }
}
