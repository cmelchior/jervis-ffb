package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

data class ReportStartingHalf(private val half: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting half: $half"
}
