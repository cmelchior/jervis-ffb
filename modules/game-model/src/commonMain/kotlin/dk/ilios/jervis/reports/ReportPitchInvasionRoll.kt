package dk.ilios.jervis.reports

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.model.Team

class ReportPitchInvasionRoll(team: Team, roll: D6Result, fanFactor: Int) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "Fans from ${team.name} invaded the pitch [ ${roll.value} + $fanFactor = ${roll.value + fanFactor} ]"
}
