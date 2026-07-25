package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way
 * from being rolled to its final result.
 */
@Serializable
@ConsistentCopyVisibility
data class D6DieRoll private constructor(
    override val id: DieId,
    override val originalRoll: D6Result,
    override var rerollSource: RerollSourceId? = null,
    override var rerolledResult: D6Result? = null,
) : DieRoll<D6Result> {

    // Work-around for `rerollSource` being an id rather than the full object
    // (Because we do not want to serialize all reroll sources)
    fun copyReroll(
        rerollSource: RerollSource? = null,
        rerolledResult: D6Result? = this.rerolledResult
    ): D6DieRoll {
        return D6DieRoll(id, originalRoll, rerollSource?.id, rerolledResult)
    }

    override val result: D6Result
        get() = rerolledResult ?: originalRoll

    companion object {
        /**
         * Create a new [D6DieRoll] for tracking a single die across rolling
         * and rerolling a dice pool.
         *
         * [indexInPool] is the index of the die in the pool. It should be stable
         * across the entire lifetime of the dice pool.
         */
        fun create(originalRoll: D6Result, indexInPool: Int = 0): D6DieRoll {
            return D6DieRoll(DieId("d6-$indexInPool"), originalRoll)
        }
    }
}
