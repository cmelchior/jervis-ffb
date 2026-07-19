package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules

class ReportStartingGame(state: Game, rules: Rules) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("Starting ${state.homeTeam.name} vs. ${state.awayTeam.name}")
        append("Using ${rules.name}")
    }
}
