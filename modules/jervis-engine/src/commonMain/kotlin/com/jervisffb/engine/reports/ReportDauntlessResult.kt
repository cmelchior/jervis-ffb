package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.block.DauntlessRollContext

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
