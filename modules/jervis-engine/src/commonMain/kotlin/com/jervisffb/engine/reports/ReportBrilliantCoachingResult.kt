package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Team

class ReportBrilliantCoachingResult(
    kickingTeam: Team,
    receivingTeam: Team,
    kickingDie: DieResult,
    kickingAssistantCoaches: Int,
    receivingDie: DieResult,
    receivingAssistantCoaches: Int,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val kickingResult = kickingDie.value + kickingAssistantCoaches
        val receivingResult = receivingDie.value + receivingAssistantCoaches
        appendLine("Brilliant Coaching: ${kickingTeam.name} [${kickingDie.value} + $kickingAssistantCoaches = $kickingResult] vs. ${receivingTeam.name} [${receivingDie.value} + $receivingAssistantCoaches = $receivingResult]")
        when {
            kickingResult > receivingResult -> append("${kickingTeam.name} wins and gets a reroll")
            receivingResult > kickingResult -> append("${receivingTeam.name} wins and gets a reroll")
            else -> append("Stand-off: Neither team gets a reroll")
        }
    }
}
