package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.PassContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportStartingPass(val pass: PassContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${pass.thrower} threw the ball" // Expand this
        }
}
