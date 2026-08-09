package com.jervisffb.engine.challenge

import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * The score value of a completed challenge. The type of value created should
 * always match the type defined in [Challenge.scoring]
 */
@Serializable
sealed interface ChallengeScore<T: ChallengeScore<T>>: Comparable<T> {
    val date: Instant
    override fun compareTo(other: T): Int

    // The challenge isn't "scored", Instead we only track if it is completed or not.
    @Serializable
    data class CompletionOnly(
        override val date: Instant
    ) : ChallengeScore<CompletionOnly> {

        override fun compareTo(other: CompletionOnly): Int {
            val scoreCompare = date.compareTo(other.date)
            return when (scoreCompare == 0) {
                true -> date.compareTo(other.date)
                false -> scoreCompare
            }
        }
    }

    // The challenge is scored based on the probability of success.
    @Serializable
    data class ProbabilityScore(
        override val date: Instant,
        val result: ProbabilityScoreResult,
    ) : ChallengeScore<ProbabilityScore> {

        override fun compareTo(other: ProbabilityScore): Int {
            val thisResult = result
            val otherResult = other.result
            require(
                thisResult.algorithmId == otherResult.algorithmId &&
                    thisResult.rerollPolicyId == otherResult.rerollPolicyId
            ) {
                "Probability Scores from different algorithm or policy versions cannot be compared."
            }

            // If scores are equal, we fall back to dates as a tiebreaker
            if (thisResult is ProbabilityScoreResult.Scored && otherResult is ProbabilityScoreResult.Scored) {
                val value = thisResult.compareTo(otherResult)
                return if (value != 0) value else date.compareTo(other.date)
            }

            // If only one is scored, this treated as before an unscored one
            return when {
                thisResult is ProbabilityScoreResult.Scored -> -1
                otherResult is ProbabilityScoreResult.Scored -> 1
                else -> date.compareTo(other.date)
            }
        }
    }
}
