package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportFoulingPlayer(val context: FoulContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.fouler.name} fouls ${context.victim!!.name}")
    }
}
