package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

/**
 * Used for report generic game progress.
 *
 * In most cases, this is probably the wrong class to use and a more specific [com.jervisffb.engine.reports.LogEntry]
 * class should be used.
 */
class ReportGameProgress(
    override val message: String,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
}
