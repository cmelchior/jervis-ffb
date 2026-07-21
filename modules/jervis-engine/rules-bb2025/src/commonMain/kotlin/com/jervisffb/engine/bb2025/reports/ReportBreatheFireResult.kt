package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.context.BreatheFireContext
import com.jervisffb.engine.bb2025.procedures.actions.block.BreatheFireResult
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportBreatheFireResult(
    context: BreatheFireContext,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val msg = when (context.result!!) {
            BreatheFireResult.ATTACKER_KNOCKED_DOWN -> {
                val player = context.attacker
                "${player.name} chokes on their own fire and is Knocked Down"
            }
            BreatheFireResult.TARGET_PLACED_PRONE -> {
                val player = context.defender!!
                "${player.name} gets slightly burned and is placed Prone"
            }
            BreatheFireResult.TARGET_KNOCKED_DOWN -> {
                val player = context.defender!!
                "${player.name} is scorched by flames and is Knocked Down"
            }
            BreatheFireResult.NO_EFFECT -> {
                val player = context.defender!!
                "Fire washes harmlessly over ${player.name}"
            }
        }
        append(msg)
    }
}
