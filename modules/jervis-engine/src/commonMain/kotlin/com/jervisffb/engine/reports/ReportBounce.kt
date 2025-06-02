package com.jervisffb.engine.reports

import com.jervisffb.engine.model.locations.FieldCoordinate

class ReportBounce(
    bounceLocation: FieldCoordinate,
    outOfBoundsAt: FieldCoordinate? = null,
    crossedLineOfScrimmageDuringKickOff: Boolean = false
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when {
            outOfBoundsAt != null && !crossedLineOfScrimmageDuringKickOff -> {
                append("Ball went out of bounds at ${outOfBoundsAt.toLogString()}.")
            }
            crossedLineOfScrimmageDuringKickOff -> {
                append("Ball crossed back to the kicking teams side in ${bounceLocation.toLogString()}")
            }
            else -> {
                append("Ball bounced to ${bounceLocation.toLogString()}")
            }
        }
    }
}
