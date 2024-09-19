package dk.ilios.jervis.reports

import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.Player

class ReportPushResult(val player: Player, val location: FieldCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${player.name} follows up to ${location.toLogString()}"
        }
}
