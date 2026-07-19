package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.Player

class ReportKickSkillResult(
    player: Player,
    d6: D6Result,
    d3: D3Result,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${player.name} uses Kick to reduce the distance from ${d6.value} (D6) to ${d3.value} (D3)")
    }
}
