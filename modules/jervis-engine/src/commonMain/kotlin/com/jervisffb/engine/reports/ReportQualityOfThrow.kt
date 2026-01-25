package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext

class ReportQualityOfThrow(
    context: ThrowTeamMateContext
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val playerName = context.thrower.name
        val message = when (context.qualityRollResult) {
            ThrowPlayerResult.SUPERB -> "$playerName made a superb throw"
            ThrowPlayerResult.SUBPAR -> "$playerName made a subpar throw"
            ThrowPlayerResult.SUCCESSFUL -> "$playerName made a successful throw"
            ThrowPlayerResult.TERRIBLE -> "$playerName made a terrible throw"
            ThrowPlayerResult.FUMBLED -> "$playerName fumbled the throw"
            null -> error("Missing quality roll value: $context")
        }
        append(message)
    }
}
