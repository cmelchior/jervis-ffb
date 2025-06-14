package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate

class ReportPushedIntoCrowd(player: Player, from: FieldCoordinate) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} is pushed into the crowd at ${from.toLogString()}"
}
