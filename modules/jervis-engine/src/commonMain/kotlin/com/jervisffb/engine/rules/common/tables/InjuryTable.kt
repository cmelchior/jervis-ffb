package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.InjuryResult
import kotlinx.serialization.Serializable

/**
 * Interface representing an Injury Table, both normal and Stunty.
 *
 * See page 60 in the 2020 rulebook.
 * See page 95 in Death Zone.
 */
@Serializable
abstract class InjuryTable {

    protected fun rollDices(firstD6: D6Result, secondD6: D6Result, modifier: Int): Int {
        return (firstD6.value + secondD6.value + modifier).coerceIn(2, 12)
    }

    /**
     * Roll on the Injury table and return the result.
     */
    abstract fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
        modifier: Int = 0,
    ): InjuryResult
}
