package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

class ReportPushResult(val player: Player, val location: FieldCoordinate, followUp: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (followUp) {
            append("${player.name} follows up to ${location.toLogString()}")
        }
    }
}
