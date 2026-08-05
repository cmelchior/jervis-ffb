package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.model.Game

/**
 * What the coach has to achieve for a [Challenge] to count as solved.
 *
 * A goal is a base goal plus a list of [modifiers] refining it, so that a
 * new restriction can be added without a new goal type for every combination.
 */
abstract class ChallengeGoal {
    /**
     * Human description of what the goal is. Used on the Challenges Details
     * Page and inside the Game UI
     */
    abstract val description: String
    /** List of modifiers that refine the goal. */
    abstract val modifiers: List<GoalModifier>

    /** Initializes any starting context for the base goal */
    abstract fun initializeBase(state: Game): ChallengeContext?

    /** Evaluates the base goal without considering any modifiers */
    abstract fun evaluateBase(state: Game, delta: GameDelta, contexts: ChallengeContextHolder): BaseGoalProgress

    /** Initializes the base goal and all modifiers */
    fun initialize(state: Game): List<ChallengeContext> {
        val baseContext = initializeBase(state)
        val modifierContexts = modifiers.map { it.initialize(state) }
        val contexts = (modifierContexts + baseContext)
        return contexts.filterNotNull()
    }

    /**
     * Evaluates the current challenge progress towards the goal and all
     * associated modifiers.  This method should be called after the game engine
     * has accepted the user action.
     *
     * [state] should be the game state after applying the user action.
     * [delta] contains information about the user action and its effects.
     * [contexts] contains all context data recorded by the previous game action.
     *
     * Any chances to a [ChallengeContext] should be returned as part of the
     * [GoalProgress].
     */
    fun evaluate(state: Game, delta: GameDelta, contexts: ChallengeContextHolder): GoalProgress {
        val baseProgress = evaluateBase(state, delta, contexts)
        val modifiersProgress = modifiers.map { modifier ->
            modifier.evaluate(state, delta, contexts)
        }
        val goalStatus = modifiersProgress
            .map { it.result }
            .fold(baseProgress.result) { first, second ->
                when {
                    (first == GoalStatus.FAILED || second == GoalStatus.FAILED) -> GoalStatus.FAILED
                    (first == GoalStatus.IN_PROGRESS || second == GoalStatus.IN_PROGRESS) -> GoalStatus.IN_PROGRESS
                    (first == GoalStatus.COMPLETED && second == GoalStatus.COMPLETED) -> GoalStatus.COMPLETED
                    else -> error("Unknown state: [$first, $second]")
                }
            }
        val updatedContexts = (modifiersProgress.mapNotNull { it.updatedContext } + baseProgress.updatedContext)
        return GoalProgress(goalStatus, updatedContexts.filterNotNull())
    }

    /** Checks that every modifier is only applied once. */
    fun validateModifiers() {
        modifiers.groupBy { it::class }
            .filterValues { it.size > 1 }
            .keys
            .firstOrNull()
            ?.let {
                throw IllegalArgumentException("${it.simpleName} is applied more than once.")
            }
    }
}
