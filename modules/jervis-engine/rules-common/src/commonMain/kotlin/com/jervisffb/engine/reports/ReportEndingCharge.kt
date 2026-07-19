package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class ReportEndingCharge(team: Team, turnOver: TurnOver?, noPlayers: Boolean = false) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (noPlayers) {
            append("No players on pitch for ${team.name}")
        } else if (turnOver != null) {
            val msg = when (turnOver) {
                TurnOver.STANDARD -> "Charge! for ${team.name} ended due to a Turnover"
                TurnOver.ACTIVE_TEAM_TOUCHDOWN,
                TurnOver.INACTIVE_TEAM_TOUCHDOWN -> INVALID_GAME_STATE("Touchdowns cannot happen during a Charge!")
                TurnOver.TIME_OUT -> TODO()
            }
            append(msg)
        } else {
            append("Charge! ended for ${team.name}")
        }
    }
}
