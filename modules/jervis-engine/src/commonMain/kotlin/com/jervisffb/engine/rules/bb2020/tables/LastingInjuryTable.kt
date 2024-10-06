package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Class representing the Lasting Injury Table on page 61 in the rulebook.
 */
object LastingInjuryTable {
    private val table: Map<Int, LastingInjuryResult> =
        mapOf(
            1 to LastingInjuryResult.HEAD_INJURY,
            2 to LastingInjuryResult.HEAD_INJURY,
            3 to LastingInjuryResult.SMASHED_KNEE,
            4 to LastingInjuryResult.BROKEN_ARM,
            5 to LastingInjuryResult.NECK_INJURY,
            6 to LastingInjuryResult.DISLOCATED_SHOULDER,
        )

    /**
     * Roll on the Lasting Injury table and return the result.
     */
    fun roll(
        d6: D6Result,
    ): LastingInjuryResult {
        val result = d6.value
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Lasting Injury Table.")
    }
}
