package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result

/**
 * Interface representing the Argue the Call table.
 *
 * See page 61 in the BB2020 rulebook.
 */
interface ArgueTheCallTable {
    /**
     * Roll on the Argue the Call table and return the result.
     */
    fun roll(d6: D6Result): ArgueTheCallResult
}
