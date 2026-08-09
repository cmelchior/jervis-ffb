package com.jervisffb.engine.statistics.probability.scorer

import com.jervisffb.engine.statistics.probability.AlgorithmId
import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.RerollUsagePolicyId
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.SurprisalAdjustment
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import kotlinx.serialization.Serializable

/**
 * Result returned by [LogicalActionPathScorer.score] and
 * [PhysicalActionPathScorer.score]. If an action path could not be scored,
 * [Unsupported] is returned with the reason why.
 */
@Serializable
sealed interface ProbabilityScoreResult {
    val algorithmId: AlgorithmId
    val rerollPolicyId: RerollUsagePolicyId
    val events: List<ActionPathEvent>

    @Serializable
    data class Scored(
        override val algorithmId: AlgorithmId,
        override val rerollPolicyId: RerollUsagePolicyId,
        override val events: List<ActionPathEvent>,
        val baseProbability: Probability,
        // Probability of the demonstrated physical trace before hypothetical recovery.
        val demonstratedProbability: Probability = baseProbability,
        val successProbability: Probability,
        val baseSurprisal: Surprisal,
        // Positive cost contributed by demonstrated activation and reroll dice.
        val actualExtraRollAdjustment: SurprisalAdjustment = SurprisalAdjustment.ZERO,
        // Non-positive benefit contributed by hypothetical recovery branches.
        val hypotheticalRecoveryAdjustment: SurprisalAdjustment = SurprisalAdjustment.ZERO,
        val rerollAdjustment: SurprisalAdjustment,
        val surprisal: Surprisal,
    ) : ProbabilityScoreResult, Comparable<Scored> {
        val eventCount: Int
            get() = events.size

        override fun compareTo(other: Scored): Int {
            return compareValuesBy(
                this,
                other,
                Scored::surprisal,
                Scored::eventCount,
            )
        }
    }

    @Serializable
    data class Unsupported(
        override val algorithmId: AlgorithmId,
        override val rerollPolicyId: RerollUsagePolicyId,
        override val events: List<ActionPathEvent>,
        val reasons: List<String>,
    ) : ProbabilityScoreResult
}
