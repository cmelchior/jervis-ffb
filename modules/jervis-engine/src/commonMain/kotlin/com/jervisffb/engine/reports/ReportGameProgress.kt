package com.jervisffb.engine.reports

/**
 * Used for report generic game progress.
 *
 * In most cases, this is probably the wrong class to use and a more specific [LogEntry]
 * class should be used.
 */
class ReportGameProgress(
    override val message: String,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
}
