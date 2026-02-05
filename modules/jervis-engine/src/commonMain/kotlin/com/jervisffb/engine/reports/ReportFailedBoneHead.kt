package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player

class ReportFailedBoneHead(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} failed their Bone Head roll and stand around looking dumbfounded"
}
