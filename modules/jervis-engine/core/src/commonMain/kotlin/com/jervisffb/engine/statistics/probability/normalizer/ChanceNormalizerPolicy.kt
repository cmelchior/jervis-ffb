package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer

/**
 * Defines how a raw [ChanceObservation] list are turned into a scoreable
 * [ActionPathEvent] list.
 *
 * See [LogicalActionPathScorer]
 * See [PhysicalActionPathScorer]
 */
interface ChanceNormalizerPolicy {
    fun normalize(observations: List<ChanceObservation>): List<ActionPathEvent>
}
