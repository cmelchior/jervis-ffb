package com.jervisffb.engine.challenge.probability

import com.jervisffb.engine.model.RerollSourceId
import kotlinx.serialization.Serializable

/**
 * The Jervis Risk Score for a sequence of rolls. Lower is better.
 *
 * [effectiveRisk] is the only thing solutions are ranked on. [benefitBySource]
 * is kept so the UI can explain where the rating came from, not so it can be
 * used as a tie-breaker.
 *
 * [algorithmVersion] is recorded because the rating is expected to change when
 * the re-roll assignment is replaced by an exact solver. Ratings produced by
 * different versions are not comparable, so anything persisted needs to know
 * which one produced it.
 */
@Serializable
data class JervisRiskScore(
    val baseRisk: Double,
    val benefitBySource: Map<RerollSourceId, Double>,
    val algorithmVersion: Int = 1 // DifficultyRating.ALGORITHM_VERSION,
) : Comparable<JervisRiskScore> {
    /** Total risk the available re-rolls cover, in bits. */
    val rerollBenefit: Double
        get() = benefitBySource.values.sum()

    /** The rating itself: how demanding the line is once re-rolls are taken into account. */
    val effectiveRisk: Double
        get() = baseRisk - rerollBenefit

    /**
     * The rating expressed as a chance of pulling the line off. Ranking by this
     * descending is the same order as ranking by [effectiveRisk] ascending, so
     * the scoreboard can show whichever reads better.
     */
    val successProbability: Double
        get() = 0.0 // riskToProbability(effectiveRisk)

    override fun compareTo(other: JervisRiskScore): Int = effectiveRisk.compareTo(other.effectiveRisk)
}
