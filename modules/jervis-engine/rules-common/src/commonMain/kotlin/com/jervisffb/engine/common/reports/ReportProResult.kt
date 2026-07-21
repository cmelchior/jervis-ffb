package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.ProRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.rules.DiceRollType

class ReportProResult(context: ProRollContext, type: DiceRollType) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (context.isSuccess) {
            append("${context.player.name} can reroll ${type.description}")
        } else {
            append("${context.player.name} failed to reroll ${type.description}")
        }
    }
}
