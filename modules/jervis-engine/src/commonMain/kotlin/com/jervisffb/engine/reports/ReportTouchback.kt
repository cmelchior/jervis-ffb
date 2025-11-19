package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation

class ReportTouchback private constructor(
    player: Player?,
    pronePlayer: Player?,
    square: FieldCoordinate?
) : LogEntry() {
    companion object {
        fun fromPlayer(player: Player) = ReportTouchback(player, null, null)
        fun fromPronePlayer(pronePlayer: Player) = ReportTouchback(null, pronePlayer, null)
        fun fromSquare(square: FieldCoordinate) = ReportTouchback(null, null, square)
    }
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (player != null) {
            append("${player.name} received the ball due to a touchback")
        } else if (pronePlayer != null) {
            val coordinates = (pronePlayer.location as OnFieldLocation).toLogString()
            append("Ball bounces from $coordinates due to a touchback given ${player?.name}")
        } else if (square != null) {
            append("Ball is placed in ${square.toLogString()} due to a touchback")
        } else {
            error("No touchback target was provided")
        }
    }
}
