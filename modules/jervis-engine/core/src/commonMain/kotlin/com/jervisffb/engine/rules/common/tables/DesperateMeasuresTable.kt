package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D8Result

/**
 * Interface representing rolling on the Desperate Measures Table.
 *
 * See page 97 in Death Zone.
 * See page 15 in Spike 22.
 */
interface DesperateMeasuresTable {
    val entries: Map<Int, DesperateMeasuresEvent>

    /**
     * Roll on the table and return the result.
     */
    fun roll(die: D8Result): DesperateMeasuresEvent
}
