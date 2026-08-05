package com.jervisffb.engine.challenge

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

    @Serializable
    data class JervisRiskScore(
        override val date: Instant,
        val score: com.jervisffb.engine.challenge.probability.JervisRiskScore
    ) : ChallengeScore<JervisRiskScore> {

        override fun compareTo(other: JervisRiskScore): Int {
            return score.compareTo(other.score)
        }
    }
}
