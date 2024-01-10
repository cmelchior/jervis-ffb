package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Team

class ReportKickingTeamResult(coinToss: Coin, kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Coin toss was $coinToss. ${kickingTeam.name} is kicking"
}
