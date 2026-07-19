package com.jervisffb.engine.reports

import com.jervisffb.engine.rules.common.procedures.rerolls.TeamCaptainRollContext
import com.jervisffb.engine.rules.common.skills.RerollSource

class ReportTeamCaptainResult(context: TeamCaptainRollContext, reroll: RerollSource) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append("${context.player.name} made ${reroll.rerollDescription} a free re-roll")
    }
}
