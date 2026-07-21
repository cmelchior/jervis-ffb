package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPushResult(val player: Player, val location: PitchCoordinate, followUp: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (followUp) {
            append("${player.name} follows up to ${location.toLogString()}")
        }
    }
}
