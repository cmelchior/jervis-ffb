package dk.ilios.jervis.reports

import dk.ilios.jervis.procedures.actions.pass.PassContext

class ReportPassResult(val pass: PassContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${pass.thrower} threw the ball" // Expand this
        }
}
