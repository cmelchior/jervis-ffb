package com.jervisffb.engine.bb2025.challenge.goal

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.challenge.BaseGoalProgress
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeGoal
import com.jervisffb.engine.challenge.GoalBuilder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.model.Game

/**
 * Goal that should only be used for debugging. It completes automatically
 * whenever a game action is triggered.
 */
class DebugGoal(
    override val modifiers: List<GoalModifier>
): ChallengeGoal() {
    override val description: String = "Trigger any game action"
    override fun initializeBase(state: Game): ChallengeContext? = null
    override fun evaluateBase(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): BaseGoalProgress {
        return BaseGoalProgress(GoalStatus.COMPLETED, null)
    }
}

class DebugGoalBuilder : GoalBuilder<DebugGoal, DebugGoalBuilder>() {
    override fun build(): DebugGoal {
        return DebugGoal(modifiers)
    }
}
