package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.TentaclesRollContext

class ReportTentaclesResult(context: TentaclesRollContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (context.isSuccess) {
            true -> {
                append("${context.tentaclePlayer!!.name}'s tentacles prevented ${context.movingPlayer.name} from moving")
            }
            false -> {
                append("${context.movingPlayer.name} slipped away from ${context.tentaclePlayer!!.name}'s greasy tentacles")
            }
        }
    }
}
