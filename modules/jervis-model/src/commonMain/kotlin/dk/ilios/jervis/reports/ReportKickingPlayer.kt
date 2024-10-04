package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player

class ReportKickingPlayer(player: Player) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.number}. ${player.name} is kicking the ball"
}
