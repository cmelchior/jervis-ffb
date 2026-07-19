package com.jervisffb.engine.reports

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.utils.sum

class ReportBrilliantCoachingResult(
    kickingTeam: Team,
    receivingTeam: Team,
    kickingDie: DieResult,
    kickingAssistantCoaches: Int,
    kickingModifiers: List<DiceModifier>,
    receivingDie: DieResult,
    receivingAssistantCoaches: Int,
    receivingModifiers: List<DiceModifier>,
) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        val kickingResult = kickingDie.value + kickingAssistantCoaches + kickingModifiers.sum()
        val receivingResult = receivingDie.value + receivingAssistantCoaches + receivingModifiers.sum()
        // Hide modifiers if none are available to reduce clutter
        append("Brilliant Coaching: ${kickingTeam.name} [${kickingDie.value} + $kickingAssistantCoaches ")
        if (kickingModifiers.isNotEmpty()) append("+ ${kickingModifiers.sum()} ")
        append("= $kickingResult] vs. ${receivingTeam.name} [${receivingDie.value} + $receivingAssistantCoaches ")
        if (receivingModifiers.isNotEmpty()) append("+ ${receivingModifiers.sum()} ")
        appendLine("= $receivingResult]")

        when {
            kickingResult > receivingResult -> append("${kickingTeam.name} wins and gets a reroll")
            receivingResult > kickingResult -> append("${receivingTeam.name} wins and gets a reroll")
            else -> append("Stand-off: Neither team gets a reroll")
        }
    }
}
