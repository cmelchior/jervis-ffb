package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules

class ReportKickResult(
    kickingTeam: Team,
    d8: D8Result,
    d6: D6Result,
    ballLocation: FieldCoordinate,
    rules: Rules,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String

    init {
        val msg =
            if (ballLocation.isOutOfBounds(rules)) {
                "Ball went out of bounds."
            } else if ((kickingTeam.isHomeTeam() && ballLocation.isOnHomeSide(rules)) ||
                (!kickingTeam.isHomeTeam() && ballLocation.isOnAwaySide(rules))
            ) {
                "Ball deviated back to the kicking teams half ${ballLocation.toLogString()}."
            } else {
                "The ball will land at ${ballLocation.toLogString()}"
            }
        this.message = msg
    }
}
