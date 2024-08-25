package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportPlayerDownResult(val attacker: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            return "${attacker.name} knocks itself down with a Player Down!"
        }
}
