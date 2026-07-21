package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.FoulContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportFoulResult(val foul: FoulContext) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String
        get() {
            val lines = mutableListOf<String>()
            if (foul.spottedByTheRef) {
                lines.add("${foul.fouler.name} fouled ${foul.victim!!.name}, but was spotted by the ref.")
            } else {
                lines.add("${foul.fouler.name} fouled ${foul.victim!!.name}")
            }
            return lines.joinToString("\n")
        }
}
