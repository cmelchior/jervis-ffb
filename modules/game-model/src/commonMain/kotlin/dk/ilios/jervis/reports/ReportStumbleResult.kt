package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportStumbleResult(val attacker: Player, val defender: Player, val fallsDown: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (fallsDown) {
            "${attacker.name} knocks down ${defender.name} with a Stumble"
        } else {
            "${defender.name} uses Dodge to avoid a Stumble"
        }
    }
}
