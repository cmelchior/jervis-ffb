package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportFailedReallyStupid(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} failed their Really Stupid roll and stand around picking their nose"
}
