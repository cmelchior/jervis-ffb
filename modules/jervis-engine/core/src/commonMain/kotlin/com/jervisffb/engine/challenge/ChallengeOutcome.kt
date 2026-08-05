package com.jervisffb.engine.challenge;

/**
 * How an attempt at a [Challenge] currently stands.
 */
enum class ChallengeOutcome {
    IN_PROGRESS, // A Challenge is in progress
    COMPLETED, // A Challenge was completed successfully
    FAILED, // A Challenge failed, i.e., the game state broke one of the rules for the challenge.
    INITIALIZING; // The Challenge has not started yet.

    val isFinished: Boolean
        get() = (this == COMPLETED || this == FAILED)
}
