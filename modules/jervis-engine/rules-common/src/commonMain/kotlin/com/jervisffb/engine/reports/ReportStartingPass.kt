package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext

class ReportStartingPass(val pass: PassContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${pass.thrower} threw the ball" // Expand this
        }
}
