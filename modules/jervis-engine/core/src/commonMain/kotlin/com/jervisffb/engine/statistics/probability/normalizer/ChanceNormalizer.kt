package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation

/**
 * Converts a list of [ChanceObservation] into a normalizer "Event Ledger" (list
 * of [ActionPathEvent]) that can be consumed by a probability evaluator.
 */
class ChanceNormalizer(
    private val policy: ChanceNormalizerPolicy,
) {
    fun normalize(observations: List<ChanceObservation>): List<ActionPathEvent> {
        return policy.normalize(observations)
    }
}
