package com.jervisffb.engine.bb2025.challenge.goal

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.BaseGoalProgress
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeGoal
import com.jervisffb.engine.challenge.GoalBuilder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.common.commands.AddTouchdown
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import kotlinx.collections.immutable.PersistentSet

data class ScoreGoalContext(
    val potentialPlayers: PersistentSet<Player>,
): ChallengeContext

/**
 * Goal that completes when a team scores a touchdown.
 */
data class ScoreTouchdownGoal(
    override val modifiers: List<GoalModifier> = emptyList(),
) : ChallengeGoal() {
    override val description: String = "Score a touchdown"

    override fun initializeBase(state: Game): ChallengeContext? = null

    override fun evaluateBase(state: Game, delta: GameDelta, contexts: ChallengeContextHolder): BaseGoalProgress {
        val isTouchdown = delta.allCommands().any { it is AddTouchdown }
        val result = when (isTouchdown) {
            true -> GoalStatus.COMPLETED
            false -> GoalStatus.IN_PROGRESS
        }
        // No need to update the context here. We only use it for its initial state
        return BaseGoalProgress(result, null)
    }
}

class ScoreTouchdownGoalBuilder: GoalBuilder<ScoreTouchdownGoal, ScoreTouchdownGoalBuilder>() {

    override fun build(): ScoreTouchdownGoal {
        return ScoreTouchdownGoal(modifiers.toList())
    }
}
