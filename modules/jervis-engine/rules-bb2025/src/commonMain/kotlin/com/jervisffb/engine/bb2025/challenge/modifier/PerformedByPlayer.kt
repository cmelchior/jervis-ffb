package com.jervisffb.engine.bb2025.challenge.modifier

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.challenge.ModifierProgress
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

/**
 * Modifies a goal, so it has to be achieved by the given [player].
 */
data class PerformedByPlayer(val player: Player) : GoalModifier {
    override val description: String = "Using ${player.name}"
    override fun initialize(state: Game): ChallengeContext? = null
    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder
    ): ModifierProgress {
        // For now, all goals need to be achieved by the active player.
        // This should match all goals that require an action, but if we want
        // to introduce goals for "inactive" players, this implementation will
        // need to change.Unfortunately, it isn't clear exactly how to do this
        // in a simple way that doesn't require strict knowledge about the base
        // goal.
        val status = when (player.id == state.activePlayer?.id) {
            true -> GoalStatus.COMPLETED
            false -> GoalStatus.IN_PROGRESS
        }
        return ModifierProgress(status, null)
    }
}
