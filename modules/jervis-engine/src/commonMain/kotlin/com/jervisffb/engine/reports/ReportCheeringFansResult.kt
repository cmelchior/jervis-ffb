package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.builder.GameVersion

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
        when (kickingTeam.game.rules.baseVersion) {
            GameVersion.BB2020 -> {
                when {
                    kickingResult > receivingResult -> append("${kickingTeam.name} wins and gets to roll on the Prayers Of Nuffle table")
                    receivingResult > kickingResult -> append("${receivingTeam.name} wins and gets to roll on the Prayers Of Nuffle table")
                    else -> append("It is a stand-off. Neither team gets to roll on the Prayers of Nuffle table.")
                }
            }
            GameVersion.BB2025 -> {
                when {
                    kickingResult > receivingResult -> append("${kickingTeam.name} wins and gets an extra Offensive Assist on the first Block in their next turn")
                    receivingResult > kickingResult -> append("${receivingTeam.name} wins gets an extra Offensive Assist on the first Block in their next turn")
                    else -> append("It is a stand-off. Both team gets an extra Offensive Assist on their next turn")
                }
            }
        }
    }
}
