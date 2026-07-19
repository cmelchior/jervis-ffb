package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.FoulContext

class ReportFoulingPlayer(val context: FoulContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.fouler.name} fouls ${context.victim!!.name}")
    }
}
