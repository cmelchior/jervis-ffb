package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.context.BeingSentOffContext
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportSpottedByRef(val context: BeingSentOffContext, val usingSecretWeapon: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (usingSecretWeapon) {
            true -> append("${context.player.name} was spotted by the ref using a secret weapon")
            false -> append("${context.player.name} was spotted by the ref")
        }
    }
}
