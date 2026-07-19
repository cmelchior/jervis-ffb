package com.jervisffb.engine.reports

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.CheeringFansContext
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.utils.sum

class ReportCheeringFansResult(
    kickingTeam: Team,
    receivingTeam: Team,
    context: CheeringFansContext,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val kickingDieRoll = context.kickingTeamRoll?.value ?: 0
        val kickingModifiers = context.kickingTeamModifiers.sum()
        val receivingDieRoll = context.receivingTeamRoll?.value ?: 0
        val receivingModifiers = context.kickingTeamModifiers.sum()
        val kickingResult = kickingDieRoll + context.kickingTeamModifiers.sum()
        val receivingResult = receivingDieRoll + context.receivingTeamModifiers.sum()
        appendLine("Cheering Fans: ${kickingTeam.name} [$kickingDieRoll + $kickingModifiers = $kickingResult] vs. ${receivingTeam.name} [$receivingDieRoll + $receivingModifiers = $receivingResult]")
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
