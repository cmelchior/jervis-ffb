package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team

class ReportKickingTeamResult(coinToss: Int, kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Coin toss was ${if (coinToss == 1) "heads" else "tails" }. ${kickingTeam.name} is kicking"
}
