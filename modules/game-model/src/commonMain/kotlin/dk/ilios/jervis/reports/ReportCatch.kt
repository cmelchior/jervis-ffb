package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportCatch(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} caught the ball"
}
