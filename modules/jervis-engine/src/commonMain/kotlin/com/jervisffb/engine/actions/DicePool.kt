package com.jervisffb.engine.actions

import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.DieRoll

/**
 * A "Dice Pool" is the main concept wrapping any pool of dice (of the same type)
 * tht is being rolled at the same time.
 *
 * Note, a roll of single die is also tracked as a dice pool, albeit a very
 * simple one.
 */
sealed interface DicePool<D: DieResult, out T: DieRoll<D>> {
    // Unique identifier for the pool. This only needs to be unique within the
    // same action step
    val id: Int
    // Which dice are part of the pool
    val dice: List<T>
    // How many dice from the pool should be selected in the end. This number
    // cannot exceed the number of total dice in the pool.
    val selectDice: Int
}

data class BlockDicePool(
    override val dice: List<BlockDieRoll>,
    override val selectDice: Int = 1,
    override val id: Int = 0
): DicePool<DBlockResult, DieRoll<DBlockResult>>

data class D6DicePool(
    override val dice: List<D6DieRoll>,
    override val selectDice: Int = 1,
    override val id: Int = 0,
): DicePool<D6Result, DieRoll<D6Result>>
