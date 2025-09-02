package com.jervisffb.engine.rules.bb2025.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.ArgueTheCallResult
import com.jervisffb.engine.rules.common.tables.ArgueTheCallTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Argue the Call table.
 *
 * See page XX in the BB 2025 rulebook.
 */
@Serializable
object BB2025ArgueTheCallTable: ArgueTheCallTable {
    private val table: Map<Int, ArgueTheCallResult> =
        mapOf(
            1 to ArgueTheCallResult.YOURE_OUTTA_HERE,
            2 to ArgueTheCallResult.I_DONT_CARE,
            3 to ArgueTheCallResult.I_DONT_CARE,
            4 to ArgueTheCallResult.I_DONT_CARE,
            5 to ArgueTheCallResult.I_DONT_CARE,
            6 to ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT,
        )

    override fun roll(
        d6: D6Result,
    ): ArgueTheCallResult {
        val result = d6.value
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Argue the Call Table.")
    }
}
