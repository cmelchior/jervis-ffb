package com.jervisffb.engine.reports

import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode

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
