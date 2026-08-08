package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.statistics.GameStatistics

/**
 * Class responsible for tracking the progress of a [challenge] attempt.
 *
 * All [ChallengeGoal] and [GoalModifier] classes are immutable and [evaluate]
 * is a pure function. So this class is responsible for keeping any necessary
 * state.
 *
 * A [ChallengeTracker] belongs to one attempt. Restarting a challenge should
 * create a new instance.
 */
class ChallengeTracker(
    private val challenge: Challenge,
) {
    private var initialized = false
    private lateinit var gameStats: GameStatistics
    private val progress = mutableListOf<ChallengeStep>()

    // List of game actions performed during the challenge
    val gameActions: List<GameAction>
        field = mutableListOf()

    // We want to track undoing separately as we can better report Challenge
    // performance.
    var undosPerformed: Int = 0
        private set

    /** Returns the outcome of the challenge, in the current game state */
    val currentOutcome: ChallengeOutcome
        get() = when (initialized) {
            false -> ChallengeOutcome.INITIALIZING
            true -> progress.lastOrNull()?.status ?: ChallengeOutcome.IN_PROGRESS
        }

    /**
     * Returns the score if the challenge was completed, or `null` if it failed
     * or are in progress
     */
    var score: ChallengeScore<*>? = null

    /**
     * Called when starting the challenge. Allows goals and modifiers to
     * record any state they need to evaluate any following states.
     */
    fun initialize(state: Game, stats: GameStatistics) {
        val goalContexts = challenge.goal.initialize(state)
        val rulesContexts = challenge.rules.map {
            it.initialize(state)
        }
        gameStats = stats
        val initialHolder = ChallengeContextHolder(goalContexts + rulesContexts)
        progress.add(ChallengeStep(ChallengeOutcome.IN_PROGRESS, initialHolder))
        initialized = true
    }

    /**
     * Updates the progress of the challenge based on the latest game action and
     * its impact on the game state.
     */
    fun evaluate(state: Game, delta: GameDelta): ChallengeOutcome {
        require(initialized) { "ChallengeTracker must be initialized first" }

        // Undo returns the original delta with its steps and commands reversed;
        // it does not contain an Undo action, but the original action.\
        if (delta.reversed) {
            // Only the initializing step is left, so this Undo is reaching into
            // the starting position. The game engine should refuse those (see
            // `GameEngineController.protectInitialActions`), so getting here means
            // something bypassed it, which is not allowed.
            if (progress.size == 1) error("Reached start of challenge. Undo not allowed")
            delta.steps.forEach { step ->
                val removed = gameActions.removeLastOrNull() ?: error("Challenge action history was empty while undoing ${step.action}")
                check(removed == step.action) {
                    "Challenge action history diverged while undoing. Expected ${step.action}, found $removed"
                }
            }
            progress.removeLast()
            undosPerformed++
            score = null
            return progress.lastOrNull()?.status ?: ChallengeOutcome.IN_PROGRESS
        }

        // Track all user and automatic actions belonging to this forward delta.
        delta.steps.forEach { step -> gameActions.add(step.action) }

        // Otherwise, run evaluation through the configured goal its modifiers
        val stepProgress = challenge.goal.evaluate(state, delta, progress.last().contexts)

        // Finally, check if any rules have been broken, which might reject the goal / modifiers claiming they are done
        val rulesProgress = challenge.rules.map { rule ->
            rule.evaluate(state, delta, progress.last().contexts)
        }
        val goalStatus = stepProgress.result
        val rulesBroken = rulesProgress.fold(false) { rulesBroken, ruleStatus ->
            rulesBroken || ruleStatus.ruleBroken
        }
        val outcome = when {
            goalStatus == GoalStatus.FAILED -> ChallengeOutcome.FAILED
            rulesBroken -> ChallengeOutcome.FAILED
            goalStatus == GoalStatus.COMPLETED && !rulesBroken -> ChallengeOutcome.COMPLETED
            else -> ChallengeOutcome.IN_PROGRESS
        }

        val updatedHolder = ChallengeContextHolder(stepProgress.updatedContexts + rulesProgress.map { it.updatedContext})
        val step = ChallengeStep(outcome, updatedHolder)

        if (outcome == ChallengeOutcome.COMPLETED) {
            score = challenge.scoring.scoreGame(state, gameActions, gameStats)
        }

        progress.add(step)

        return outcome
    }
}
