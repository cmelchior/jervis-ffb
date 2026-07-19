package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Team

class ReportKickingTeamResult(coinToss: Coin, kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Coin toss was $coinToss. ${kickingTeam.name} is kicking"
}
