package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.model.Game

/**
 * Refines a [ChallengeGoal], e.g. "<BaseGoal> by player A3" or
 * "<BaseGoal> with 2 block dice".
 *
 * Currently, there is no way programmatically to figure out which modifiers
 * are applicable to a goal. This will probably need to change once a proper
 * Challenge Editor UI is added, but it is unclear exactly what that API can
 * look like if we want to support having modifiers in different rules modules.
 */
interface GoalModifier {

    /** Reads as a continuation of the goal description, e.g. "with 2 block dice". */
    val description: String

    /** Initializes the starting context for this modifier */
    fun initialize(state: Game): ChallengeContext?

    /**
     * Evaluates this modifier against the current game state. This will be
     * called even if the base [ChallengeGoal] is [ChallengeOutcome.FAILED].
     */
    fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): ModifierProgress
}
