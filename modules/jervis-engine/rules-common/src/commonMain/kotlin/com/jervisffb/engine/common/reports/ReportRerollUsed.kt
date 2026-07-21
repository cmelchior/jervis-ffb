package com.jervisffb.engine.common.reports

import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.rules.common.skills.RerollSource

class ReportRerollUsed(
    reroll: RerollSource,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = "${reroll.rerollDescription} used"
}
