package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportKickingTeamResult(coinToss: Coin, kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Coin toss was $coinToss. ${kickingTeam.name} is kicking"
}
