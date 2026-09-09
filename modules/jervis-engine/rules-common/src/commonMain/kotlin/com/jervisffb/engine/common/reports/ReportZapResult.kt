package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.procedures.inducements.spells.ZapContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportZapResult(context: ZapContext): LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = when (context.isSuccess) {
        true -> "ZAP! transformed ${context.target.name} into a frog"
        false -> "ZAP! failed to transform ${context.target.name}"
    }
}
