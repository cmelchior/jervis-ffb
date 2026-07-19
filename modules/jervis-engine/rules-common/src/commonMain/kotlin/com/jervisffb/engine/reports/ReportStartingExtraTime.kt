package com.jervisffb.engine.reports

object ReportStartingExtraTime : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Starting Extra Time"
}
