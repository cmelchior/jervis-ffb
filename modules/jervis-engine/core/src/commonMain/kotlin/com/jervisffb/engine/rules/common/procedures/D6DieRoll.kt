package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
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
        fun create(state: Game, originalRoll: D6Result): D6DieRoll {
            return D6DieRoll(state.idGenerator.nextDiceId(), originalRoll)
        }
    }
}
