package com.jervisffb.engine.challenge

import com.jervisffb.engine.statistics.probability.scorer.ActionPathScorer

/**
 * Describing how rerolls are selected when needed during the challenge, when
 * using [ChallengeScoring.ProbabilityScoring].
 */
sealed interface ChallengeRerollSelectionPolicy {
    val stateCeiling: Int

    // Rerolls chosen by the user are ignored, and they are instead distributed
    // by the policy.
    data class LogicalRerollSelection(
        override val stateCeiling: Int = ActionPathScorer.DEFAULT_STATE_CEILING,
    ) : ChallengeRerollSelectionPolicy {
        init {
            require(stateCeiling > 0) { "State ceiling must be positive: $stateCeiling" }
        }
    }

    // Rerolls chosen by the coach are respected, the remaining are distributed
    // by the policy.
    data class PhysicalRerollSelection(
        override val stateCeiling: Int = ActionPathScorer.DEFAULT_STATE_CEILING,
    ) : ChallengeRerollSelectionPolicy {
        init {
            require(stateCeiling > 0) { "State ceiling must be positive: $stateCeiling" }
        }
    }
}
