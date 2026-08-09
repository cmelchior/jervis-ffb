package com.jervisffb.engine.statistics.probability.scorer

import com.jervisffb.engine.statistics.probability.RerollUsagePolicyId
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.RerollOption

/**
 * A policy that determines which (hypothetical) reroll to use at a given point
 * in a [ActionPathEvent] sequence. This is used to calculate the probability
 * of a given [ActionPathEvent] sequence.
 *
 * Note: Right now this interface only really supports "fixed" reroll policies,
 * if we ever want to support a more complex reroll policy, it likely needs to
 * be redesigned.
 */
interface RerollUsagePolicy {
    /**
     * Identifier for this policy. Scores created using different policies are
     * not comparable.
     *
     * This also means that any chance to a policy requires a new `id`, so we
     * don't accidentally compare a newer policy to an older one.
     */
    val id: RerollUsagePolicyId

    /**
     * Select the reroll option to attempt from the rerolls currently available.
     */
    fun select(options: List<RerollOption>): RerollOption?
}
