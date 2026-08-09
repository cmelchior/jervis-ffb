package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.statistics.probability.Probability
import kotlinx.serialization.Serializable

/**
 * An exact ratio retained by probability scorers.
 *
 * Ratios are retained in the ledger so selected dice values do not acquire
 * rounding errors. The dynamic program converts them to [Probability] only
 * while evaluating the complete action path.
 */
@Serializable
data class OutcomeRatio(
    val favorableOutcomes: Int,
    val possibleOutcomes: Int,
) {
    init {
        require(possibleOutcomes > 0) { "The number of possible outcomes must be positive." }
        require(favorableOutcomes in 0..possibleOutcomes) {
            "Favorable outcomes must be between 0 and $possibleOutcomes: $favorableOutcomes"
        }
    }

    val probability: Probability
        get() = Probability(favorableOutcomes.toDouble() / possibleOutcomes)

    companion object {
        val CERTAIN = OutcomeRatio(1, 1)
    }
}

