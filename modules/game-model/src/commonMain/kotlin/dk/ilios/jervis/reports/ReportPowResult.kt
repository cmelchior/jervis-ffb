package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportPowResult(val attacker: Player, val defender: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${attacker.name} knocks down ${defender.name} with a POW!"
        }
}
