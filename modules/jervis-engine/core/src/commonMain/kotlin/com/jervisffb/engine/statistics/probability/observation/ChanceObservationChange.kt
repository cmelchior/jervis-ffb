package com.jervisffb.engine.statistics.probability.observation

/**
 * A reversible change to the chance-observation stream.
 */
sealed interface ChanceObservationChange {
    val observation: ChanceObservation

    data class Recorded(
        override val observation: ChanceObservation,
    ) : ChanceObservationChange

    data class Updated(
        override val observation: ChanceObservation,
        val previous: ChanceObservation,
    ) : ChanceObservationChange

    data class Removed(
        override val observation: ChanceObservation,
    ) : ChanceObservationChange

    data class Restored(
        override val observation: ChanceObservation,
    ) : ChanceObservationChange
}
