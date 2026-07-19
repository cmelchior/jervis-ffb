package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.foul.BeingSentOffContext

class ReportSpottedByRef(val context: BeingSentOffContext, val usingSecretWeapon: Boolean) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        when (usingSecretWeapon) {
            true -> append("${context.player.name} was spotted by the ref using a secret weapon")
            false -> append("${context.player.name} was spotted by the ref")
        }
    }
}
