package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportGoingIntoSuddenDeath(private val state: Game) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        appendLine("${state.homeTeam.name} draws ${state.awayScore} : ${state.homeScore} against ${state.awayTeam.name} after extra time")
        append("Game goes into Sudden Death")
    }

}

