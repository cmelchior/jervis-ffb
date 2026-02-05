package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportFailedReallyStupid(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} failed their Really Stupid roll and stand around picking their nose"
}
