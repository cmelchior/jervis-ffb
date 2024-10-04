package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportTouchback(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} received the ball due to a touchback."
}
