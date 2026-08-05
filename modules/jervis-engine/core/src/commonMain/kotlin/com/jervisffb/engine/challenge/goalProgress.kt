package com.jervisffb.engine.challenge

/**
 * This file contains classes for tracking challenge progress across varios
 * parts.
 */

/** Progress on a [ChallengeGoal] without considering its modifiers. */
data class BaseGoalProgress(
    val result: GoalStatus,
    val updatedContext: ChallengeContext?
)

/** Progress on a [GoalModifier] */
data class ModifierProgress(
    val result: GoalStatus,
    val updatedContext: ChallengeContext?
)

/** Progress on a [ChallengeGoal] including both base goal AND its modifiers. */
data class GoalProgress(
    val result: GoalStatus,
    val updatedContexts: List<ChallengeContext>
)

/** Progress on a [ChallengeRule] */
data class RuleProgress(
    val ruleBroken: Boolean,
    val updatedContext: ChallengeContext?
)


