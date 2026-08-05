package com.jervisffb.engine.challenge

/**
 * Used to track each step in a Challenge. This should contain an immutable
 * snapshot of the current state, allowing the [ChallengeTracker] to support
 * undoing without having to re-implement the Undo system from the game
 * engine.
 *
 * It is assumed that the number of [ChallengeContext] classes being tracked is
 * relatively low (x < 10).
 */
data class ChallengeStep(
    val status: ChallengeOutcome,
    val contexts: ChallengeContextHolder,
)
