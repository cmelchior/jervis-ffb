package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result

/**
 * Interface representing the Lasting Injury Table.
 *
 * See page 61 in the BB2020 rulebook.
 * See page 67 in the BB2025 rulebook.
 */
interface LastingInjuryTable {
    /**
     * Roll on the Lasting Injury table and return the result.
     */
    fun roll(d6: D6Result): LastingInjuryResult
}
