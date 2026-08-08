package com.jervisffb.engine.statistics.probability

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.math.log2
import kotlin.math.pow

/**
 * The surprisal (in bits) is a derived value from the probability of a
 * specific event occurring from a random variable. Surprisal is always >= 0.0.
 *
 * Jervis uses this unit to calculate the final success chance for a chain of
 * dice rolls (or other events with random chance). Two properties make it a
 * good fit: it is additive across independent rolls, and one bit (1.0) is
 * exactly one 4+ roll.
 *
 * As a logarithmic unit, for each additional bit, the probability of success
 * is halved. This means:
 *
 * 0.0: 100% chance of success
 * 1.0: 50% chance of success
 * 2.0: 25% chance of success
 * 3.0: 12.5% chance of success
 * 4.0: 6.25% chance of success
 * 5.0: 3.125% chance of success
 * 6.0: 1.5625% chance of success
 *
 * See https://en.wikipedia.org/wiki/Information_content
 */
@Serializable
@JvmInline
value class Surprisal private constructor(val value: Double): Comparable<Surprisal> {
    init {
        require(value >= 0.0) { "Surprisal must be >= 0: $value" }
    }

    operator fun plus(other: Surprisal): Surprisal {
        return Surprisal(value + other.value)
    }

    operator fun minus(other: Surprisal): Surprisal {
        return Surprisal(value - other.value)
    }

    fun toProbability(): Probability {
        return Probability(if (value == 0.0) 0.0 else 2.0.pow(-value))
    }

    override fun compareTo(other: Surprisal): Int = other.value.compareTo(value)

    companion object {
        val ZERO = Surprisal(0.0)
        operator fun invoke(value: Double): Surprisal {
            return Surprisal(if (value == -0.0) 0.0 else value)
        }
    }
}

/**
 * A calculated Surprisal value that doesn't represent a probability by itself,
 * but just the difference between two probabilities. This is allowed to be
 * outside the normal legal range of [Surprisal].
 */
@JvmInline
@Serializable
value class SurprisalAdjustment private constructor(val value: Double) {

    operator fun plus(other: SurprisalAdjustment): SurprisalAdjustment {
        return SurprisalAdjustment(value + other.value)
    }

    operator fun minus(other: SurprisalAdjustment): SurprisalAdjustment {
        return SurprisalAdjustment(value - other.value)
    }

    companion object {
        val ZERO = SurprisalAdjustment(0.0)
        fun from(from: Surprisal, target: Surprisal): SurprisalAdjustment {
            return SurprisalAdjustment(target.value - from.value)
        }
    }
}



/**
 * Inverse of [Surprisal]. This represents the probability of an event being a
 * success. In Jervis this is used to represent the chance of rolling dice
 * values.
 *
 * Probability is always in the range [0.0, 1.0].
 *
 * See [Surprisal] for more details.
 */
@Serializable
@JvmInline
value class Probability(val value: Double): Comparable<Probability> {
    init {
        require(value in (0.0 .. 1.0)) { "Probability must be in the range [0.0, 1.0]: $value" }
    }

    operator fun times(other: Probability): Probability {
        return Probability(value * other.value)
    }

    operator fun plus(other: Probability): Probability {
        return Probability(value + other.value)
    }

    operator fun minus(other: Probability): Probability {
        return Probability(value - other.value)
    }

    fun pow(exponent: Int): Probability {
        return Probability(value.pow(exponent))
    }

    fun toSurprisal(): Surprisal {
        return Surprisal(if (value == 0.0) 0.0 else -log2(value))
    }

    // Returns the inverse probability of this probability, e.g. if it represents
    // 40% (for success), it returns 60% (for failure).
    fun inverse(): Probability {
        return Probability(1.0 - value)
    }

    override fun compareTo(other: Probability): Int = value.compareTo(other.value)

    companion object {
        val ALWAYS = Probability(1.0)
        val NEVER = Probability(0.0)
        operator fun invoke(value: Double): Probability {
            return Probability(if (value == -0.0) 0.0 else value)
        }
    }
}

