package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext

class ReportGoal(state: Game, context: ScoringATouchDownContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.player.name} scored a touchdown")
    }
}
