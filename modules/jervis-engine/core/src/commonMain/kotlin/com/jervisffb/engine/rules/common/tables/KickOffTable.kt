package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result

/**
 * Interface representing a Kick-off Table.
 *
 * See page 48 in the BB2025 rulebook.
 */
interface KickOffTable {
    /**
     * All table entries by the sum of the dice.
     */
    val entries: Map<Int, KickOffEvent>

    /**
     * Name of the table.
     */
    val name: String

    /**
     * Roll on the Kick-Off table and return the result.
     */
    fun roll(
        die1: D6Result,
        die2: D6Result,
    ): KickOffEvent
}
