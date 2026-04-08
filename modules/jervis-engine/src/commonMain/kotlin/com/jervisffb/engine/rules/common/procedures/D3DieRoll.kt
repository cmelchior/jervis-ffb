package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Wrap a single D3 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
@Serializable
@ConsistentCopyVisibility
data class D3DieRoll private constructor(
    override val id: DieId,
    override val originalRoll: D3Result,
    override var rerollSource: RerollSourceId? = null,
    override var rerolledResult: D3Result? = null,
) : DieRoll<D3Result> {

    // Work-around for `rerollSource` being an id rather than the full object
    // (Because we do not want to serialize all reroll sources)
    fun copyReroll(
        rerollSource: RerollSource? = null,
        rerolledResult: D3Result? = this.rerolledResult
    ): D3DieRoll {
        return D3DieRoll(id, originalRoll, rerollSource?.id, rerolledResult)
    }

    override val result: D3Result
        get() = rerolledResult ?: originalRoll

    companion object {
        fun create(state: Game, originalRoll: D3Result): D3DieRoll {
            return D3DieRoll(state.idGenerator.nextDiceId(), originalRoll)
        }
    }
}
