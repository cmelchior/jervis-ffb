package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.skills.RerollSource

/**
 * Interface allowing us to track a single die all the way from rolling it
 * the first time to it getting rerolled. This includes both single die rolls as
 * well as rolls involving multiple dice (dice pools).
 */
sealed interface DieRoll<D : DieResult> {
    val id: DieId
    val originalRoll: D
    var rerollSource: RerollSource?
    var rerolledResult: D?
    val result: D
}

/**
 * Wrap a single Block die roll. This makes it possible to track it all the way from being rolled to its final result
 */
data class BlockDieRoll(
    override val id: DieId,
    override val originalRoll: DBlockResult,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: DBlockResult? = null,
) : DieRoll<DBlockResult> {
    override val result: DBlockResult
        get() = rerolledResult ?: originalRoll
}

/**
 * Wrap a single D6 die roll. This makes it possible to track it all the way from being rolled to its final result.
 */
data class D6DieRoll(
    override val id: DieId,
    override val originalRoll: D6Result,
    override var rerollSource: RerollSource? = null,
    override var rerolledResult: D6Result? = null,
) : DieRoll<D6Result> {
    override val result: D6Result
        get() = rerolledResult ?: originalRoll

    companion object {
        fun create(state: Game, originalRoll: D6Result): D6DieRoll {
            return D6DieRoll(state.idGenerator.nextDiceId(), originalRoll)
        }
    }
}


