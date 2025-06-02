package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.OnFieldLocation

class ReportTouchback(player: Player, pronePlayer: Boolean = false) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (!pronePlayer) {
            append("${player.name} received the ball due to a touchback.")
        } else {
            val coordinates = (player.location as OnFieldLocation).toLogString()
            append("Ball bounces from $coordinates due to a touchback given ${player.name}")
        }
    }
}
