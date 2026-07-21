package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportSteadyFootingResult(context: SteadyFootingRollContext, mode: RiskingInjuryMode) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append(context.player.name)
        append(" avoided ")
        when (mode) {
            RiskingInjuryMode.FALLING_OVER -> append("Falling Over")
            RiskingInjuryMode.KNOCKED_DOWN -> append("being Knocked Down")
            else -> error("Unsupported mode: $mode")
        }
        append(" using Steady Footing")
    }
}
