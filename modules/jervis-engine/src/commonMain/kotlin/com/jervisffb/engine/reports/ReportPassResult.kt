package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.bb2020.procedures.actions.pass.PassContext

class ReportPassResult(val pass: PassContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${pass.thrower} threw the ball" // Expand this
        }
}
