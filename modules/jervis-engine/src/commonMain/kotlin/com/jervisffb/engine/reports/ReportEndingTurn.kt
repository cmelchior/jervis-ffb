package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver

class ReportEndingTurn(team: Team, turn: Int, turnOver: TurnOver?) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (turnOver != null) {
            val msg = when (turnOver) {
                TurnOver.STANDARD -> "Turn $turn for ${team.name} ended due to a Turnover"
                TurnOver.ACTIVE_TEAM_TOUCHDOWN -> "Turn $turn for ${team.name} ended due to a Touchdown"
                TurnOver.INACTIVE_TEAM_TOUCHDOWN -> "Turn $turn for ${team.name} ended due to a Touchdown against them."
            }
            append(msg)
        } else {
            append("Ending turn $turn for ${team.name}")
        }
    }
}
