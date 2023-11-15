package dk.ilios.bowlbot.logs

import dk.ilios.bowlbot.model.Team

class ReportStartingKickOff(kickingTeam: Team) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${kickingTeam.name} is kicking off"
}
