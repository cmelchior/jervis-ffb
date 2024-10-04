package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction

class ReportActionEnded(player: Player, action: PlayerAction) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} ended ${action.name}."
}
