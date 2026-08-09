package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.statistics.probability.event.ChanceOutcomeCategory
import com.jervisffb.engine.statistics.probability.event.OutcomeRatio
import kotlinx.serialization.Serializable

/**
 * The objective against which the observed result was evaluated.
 *
 * The ratio describes the probability of satisfying the objective, not the
 * probability of the particular physical face that happened to be rolled.
 * Rule procedures calculate this because they have the context needed to
 * distinguish, for example, a Charge capacity from an exact target result.
 */
@Serializable
data class ChanceOutcome(
    val category: ChanceOutcomeCategory,
    val successProbability: OutcomeRatio,
)
