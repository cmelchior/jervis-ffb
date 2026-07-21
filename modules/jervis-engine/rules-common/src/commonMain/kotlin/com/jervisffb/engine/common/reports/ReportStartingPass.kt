package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.common.procedures.actions.pass.PassContext

class ReportStartingPass(val pass: PassContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${pass.thrower} threw the ball" // Expand this
        }
}
