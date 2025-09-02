package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.common.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
@Serializable
@ConsistentCopyVisibility
data class BlockDieRoll private constructor(
    override val id: DieId,
    override val originalRoll: DBlockResult,
    override var rerollSource: RerollSourceId? = null,
    override var rerolledResult: DBlockResult? = null,
) : DieRoll<DBlockResult> {
    override val result: DBlockResult
        get() = rerolledResult ?: originalRoll

    // Work-around for `rerollSource` being an id rather than the full object
    // (Because we do not want to serialize all reroll sources)
    fun copyReroll(
        rerollSource: RerollSource? = null,
        rerolledResult: DBlockResult? = this.rerolledResult
    ): BlockDieRoll {
        return BlockDieRoll(id, originalRoll, rerollSource?.id, rerolledResult)
    }

    companion object {
        fun create(state: Game, originalRoll: DBlockResult): BlockDieRoll {
            return BlockDieRoll(state.idGenerator.nextDiceId(), originalRoll)
        }
    }
}
