package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.rerolls.ProRollContext

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
