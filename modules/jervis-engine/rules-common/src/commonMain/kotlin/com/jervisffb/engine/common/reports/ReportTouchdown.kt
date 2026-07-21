package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportTouchdown(state: Game, context: ScoringATouchDownContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.player.name} scored a touchdown")
    }
}
