package com.jervisffb.engine.statistics.probability.scorer

import com.jervisffb.engine.statistics.probability.AlgorithmId
import com.jervisffb.engine.statistics.probability.RerollUsagePolicyId
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.SurprisalAdjustment
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import kotlinx.serialization.Serializable

/**
 * Result returned by [LogicalActionPathScorer.score] and
 * [PhysicalActionPathScorer.score].
 *
 * If an action path could not be scored, [Unsupported] is returned with the
 * reason why.
 */
@Serializable
sealed interface ProbabilityScoreResult {
    val algorithmId: AlgorithmId
    val rerollPolicyId: RerollUsagePolicyId
    val events: List<ActionPathEvent>

    /**
     * A successfully scored action path.
     *
     * The result contains three different probabilities. They describe
     * progressively larger interpretations of the same path:
     *
     * 1. [baseProbability] is the scorer-specific baseline before hypothetical
     *    recoveries are considered. For [PhysicalActionPathScorer], it is the
     *    probability of the primary action rolls: activation and reroll dice
     *    are intentionally excluded. For [LogicalActionPathScorer], it is the
     *    product of the observed probabilities of every event that remains
     *    after normalization. The fixed normalizer omits activation events
     *    and collapses rerolls into their parent event, so the logical scorer
     *    does not calculate a separate physical cost for those extra dice.
     * 2. [demonstratedProbability] is the probability of the exact physical
     *    trace that was observed, including activation rolls and reroll dice.
     *    It does not include rerolls that were merely available but not used.
     * 3. [successProbability] is the probability after expanding the path
     *    with hypothetical recoveries allowed by the selected scorer and
     *    reroll policy. It sums the valid resulting reroll states, so it can be
     *    higher than [demonstratedProbability]. It is not necessarily the
     *    probability of the exact trace that was played.
     *
     * Example: A physical path uses Pro to reroll a failed Dodge. Pro succeeds
     * and the Dodge is rerolled. A team reroll is also available.
     *
     * For [PhysicalActionPathScorer], this means:
     *
     * - [baseProbability] contains the primary Dodge roll but excludes the
     *   Pro activation roll and the Pro reroll of the Dodge.
     * - [demonstratedProbability] includes both the Pro activation roll and
     *   the Dodge reroll because they actually happened.
     * - [successProbability] may also include a hypothetical branch in which
     *   the available team reroll is used to reroll a failed Pro activation.
     *   That branch is included only if the normalized event exposes the
     *   option and the scorer's policy permits it. It does not mean that the
     *   team reroll was actually used.
     *
     * For [LogicalActionPathScorer] this means:
     *
     * - [baseProbability] contains the probability of the final Dodge result
     *   after the reroll. The Pro activation is omitted and the Dodge reroll is
     *   folded into the Dodge event so it only shows the final value.
     * - [demonstratedProbability] is the same as [baseProbability]. Although
     *   the physical path used Pro, the logical scorer does not preserve that
     *   activation or reroll as separate events.
     * - [successProbability] evaluates the recovery choices attached to the
     *   original Dodge event according to the logical scorer's policy. In this
     *   example, that may include choosing the available team reroll instead
     *   of Pro. It describes a (possible) alternative logical line.
     */
    @Serializable
    data class Scored(
        override val algorithmId: AlgorithmId,
        override val rerollPolicyId: RerollUsagePolicyId,
        override val events: List<ActionPathEvent>,
        /**
         * The surprisal before actual-roll and hypothetical-recovery
         * adjustments.
         */
        val baseSurprisal: Surprisal,
        /**
         * The surprisal of the observed trace before hypothetical recovery.
         * Is equal to [baseSurprisal] for scorers that collapse roll + reroll
         * into on event.
         */
        val demonstratedSurprisal: Surprisal = baseSurprisal,
        /**
         * The surprisal of the scorer's final success probability after
         * hypothetical recovery branches have been evaluated.
         */
        val successSurprisal: Surprisal,
        /**
         * The positive surprisal cost of activation and reroll dice that were
         * actually demonstrated. This is the difference between the
         * unadjusted and demonstrated probabilities. It is normally zero
         * when using [LogicalActionPathScorer].
         *
         * This adjustment will result in a lower probability.
         */
        val actualExtraRollAdjustment: SurprisalAdjustment = SurprisalAdjustment.ZERO,
        /**
         * The improvement to surprisal from the demonstrated path to the final
         * path after hypothetical recoveries.
         *
         * This adjustment will result in a higher probability.
         */
        val hypotheticalRecoveryAdjustment: SurprisalAdjustment = SurprisalAdjustment.ZERO,
        /**
         * The total reroll-related adjustment. For [PhysicalActionPathScorer]
         * this is [actualExtraRollAdjustment] plus [hypotheticalRecoveryAdjustment].
         * For [LogicalActionPathScorer] it is the adjustment from the unadjusted
         * probability directly to the final probability.
         */
        val rerollAdjustment: SurprisalAdjustment,
    ) : ProbabilityScoreResult, Comparable<Scored> {

        /** Probability represented by [baseSurprisal]. */
        val baseProbability get() = baseSurprisal.toProbability()

        /** Probability of the exact observed trace represented by [demonstratedSurprisal]. */
        val demonstratedProbability get() = demonstratedSurprisal.toProbability()

        /** Final probability after the scorer evaluates permitted hypothetical recoveries. */
        val successProbability get() = successSurprisal.toProbability()

        val eventCount: Int
            get() = events.size

        // When comparing scores, we always use success probability with
        // number of events as a tie-breaker.
        override fun compareTo(other: Scored): Int {
            return compareValuesBy(
                this,
                other,
                Scored::successProbability,
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
