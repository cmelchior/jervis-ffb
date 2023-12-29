package dk.ilios.jervis.logs

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.rules.Rules

class ReportKickResult(
    d8: D8Result,
    d6: D6Result,
    ballLocation: FieldCoordinate,
    rules: Rules
) : LogEntry() {

    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String

    init {
        var msg = "Rolled [d8=${d8.result}, d6=${d6.result}]."
        msg = if (ballLocation.isOutOfBounds(rules)) {
            "$msg Ball went out of bounds."
        } else {
            "$msg Will land at [${ballLocation.x}, ${ballLocation.y}]"
        }
        this.message = msg
    }
}
