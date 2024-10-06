package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Team

class ReportCheeringFansResult(
    kickingTeam: Team,
    receivingTeam: Team,
    dieKickingTeam: DieResult,
    cheerLeadersKickingTeam: Int,
    dieReceivingTeam: DieResult,
    cheerLeadersReceivingTeam: Int,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val kickingResult = dieKickingTeam.value + cheerLeadersKickingTeam
        val receivingResult = dieReceivingTeam.value + cheerLeadersReceivingTeam
        appendLine("Cheering Fans: ${kickingTeam.name} [${dieKickingTeam.value} + $cheerLeadersKickingTeam = $kickingResult] vs. ${receivingTeam.name} [${dieReceivingTeam.value} + $cheerLeadersReceivingTeam = $receivingResult]")
        when {
            kickingResult > receivingResult -> append("${kickingTeam.name} wins and gets to roll on the Prayers Of Nuffle table.")
            receivingResult > kickingResult -> append("${receivingTeam.name} wins and gets to roll on the Prayers Of Nuffle table.")
            else -> append("It is a stand-off. Neither team gets to roll on the Prayers of Nuffle table.")
        }
    }
}
