package com.jervisffb.engine.common.reports

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportTouchback private constructor(
    player: Player?,
    pronePlayer: Player?,
    square: PitchCoordinate?
) : LogEntry() {
    companion object {
        fun fromPlayer(player: Player) = ReportTouchback(player, null, null)
        fun fromPronePlayer(pronePlayer: Player) = ReportTouchback(null, pronePlayer, null)
        fun fromSquare(square: PitchCoordinate) = ReportTouchback(null, null, square)
    }
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        if (player != null) {
            append("${player.name} received the ball due to a touchback")
        } else if (pronePlayer != null) {
            val coordinates = (pronePlayer.location as OnPitchLocation).toLogString()
            append("Ball bounces from $coordinates due to a touchback given ${player?.name}")
        } else if (square != null) {
            append("Ball is placed in ${square.toLogString()} due to a touchback")
        } else {
            error("No touchback target was provided")
        }
    }
}
