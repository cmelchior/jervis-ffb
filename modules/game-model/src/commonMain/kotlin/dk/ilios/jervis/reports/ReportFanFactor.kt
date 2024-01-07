package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Team

class ReportFanFactor(team: Team, diceRoll: Int, dedicatedFans: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${team.name} rolled $diceRoll. [$diceRoll + $dedicatedFans = ${diceRoll+dedicatedFans}k] fans are cheering on."
}
