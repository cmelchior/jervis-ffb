package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportFireballHit(team: Team, size: Int): LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val msg = when {
            size == 0 -> "A fireball from ${team.name} lands harmlessly on the ground"
            size == 1 -> "A fireball from ${team.name} engulfs 1 player"
            size >= 2 -> "A fireball from ${team.name} engulfs $size players"
            else -> error("Invalid size: $size")
        }
        append(msg)
    }
}
