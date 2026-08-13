package com.jervisffb.engine.bb2025.challenge.modifier

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.challenge.ModifierProgress
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

/**
 * Modifies a goal, so it has to be achieved by the given [team].
 */
data class PerformedByTeam(val team: Team) : GoalModifier {
    override val description: String = "Using any player on ${team.name}"
    override fun initialize(state: Game): ChallengeContext? = null
    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): ModifierProgress {
        val owner = delta.owner ?: return ModifierProgress(GoalStatus.IN_PROGRESS, null)
        val actionTeam = state.getTeam(owner)
        val status = when (actionTeam.id == team.id) {
            true -> GoalStatus.COMPLETED
            false -> GoalStatus.IN_PROGRESS
        }
        return ModifierProgress(status, null)
    }
}
