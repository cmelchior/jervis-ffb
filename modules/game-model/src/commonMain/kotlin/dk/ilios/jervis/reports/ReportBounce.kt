package dk.ilios.jervis.reports

import dk.ilios.jervis.model.FieldCoordinate

class ReportBounce(bounceLocation: FieldCoordinate, outOfBoundsAt: FieldCoordinate? = null) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String =
        if (outOfBoundsAt != null) {
            "Ball went out of bounds at ${outOfBoundsAt.toLogString()}."
        } else {
            "Ball bounced to ${bounceLocation.toLogString()}"
        }
}
