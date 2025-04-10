package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import kotlinx.serialization.Serializable

/**
 * Interface allowing us to track a single die all the way from rolling it
 * the first time to it getting rerolled. This includes both single die rolls as
 * well as rolls involving multiple dice (dice pools).
 */
@Serializable
sealed interface DieRoll<D : DieResult> {
    val id: DieId
    val originalRoll: D
    var rerollSource: RerollSourceId?
    var rerolledResult: D?
    val result: D
}

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
@Serializable
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

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
@Serializable
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


