package com.jervisffb.engine.actions

import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll

// Give a number of dice pools, the user needs to select 1 or more of them
sealed interface DicePool<D: DieResult, out T: DieRoll<D>> {
    val id: Int
    val dice: List<T>
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
