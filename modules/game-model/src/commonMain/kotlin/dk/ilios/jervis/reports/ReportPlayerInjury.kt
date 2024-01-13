package dk.ilios.jervis.reports

import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

class ReportPlayerInjury(player: Player, state: PlayerState) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${player.name} is ${state.name}."
}
