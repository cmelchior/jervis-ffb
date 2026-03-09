package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportFailedTakeRoot(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} failed their Take Root roll and are now Rooted"
}
