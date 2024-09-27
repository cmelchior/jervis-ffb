package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.locations.FieldCoordinate

class ReportPushResult(val player: Player, val location: FieldCoordinate, followUp: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (followUp) {
            append("${player.name} follows up to ${location.toLogString()}")
        }
    }
}
