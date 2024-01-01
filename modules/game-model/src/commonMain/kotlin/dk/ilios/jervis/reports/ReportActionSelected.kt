package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.PlayerAction

class ReportActionSelected(player: Player, action: PlayerAction) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} selected action: ${action.name}."
}
