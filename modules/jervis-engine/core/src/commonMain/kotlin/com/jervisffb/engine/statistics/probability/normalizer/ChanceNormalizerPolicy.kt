package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.scorer.ActionPathScorer
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
    /**
     * Roll types representing the primary action being scored.
     * "Primary" in this case, means any rolls that we always want to include
     * in the final probability score.
     */
    val primaryRollTypes: Set<DiceRollType>

    /**
     * Roll types representing activation checks associated with activatig a
     * reroll. Depending on the policy, these rolls might be ignored
     * to let the [ActionPathScorer] choose how to use them optimally.
     */
    val activationRollTypes: Set<DiceRollType>

    /**
     * Roll types that should not contribute to the final probability score.
     * These rolls will be removed during normalization.
     */
    val ignoredRollTypes: Set<DiceRollType>

    /**
     * Convert the [ChanceObservation] list into a scoreable [ActionPathEvent]
     * list. This process can filter or combine observations preparing depending
     * on the supported roll types and the implemented policy.
     */
    fun normalize(observations: List<ChanceObservation>): List<ActionPathEvent>
}
