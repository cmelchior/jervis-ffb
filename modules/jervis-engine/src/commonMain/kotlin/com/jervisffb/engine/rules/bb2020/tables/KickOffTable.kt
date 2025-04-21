package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result

/**
 * Interface representing a Kick-off Table.
 */
interface KickOffTable {
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
