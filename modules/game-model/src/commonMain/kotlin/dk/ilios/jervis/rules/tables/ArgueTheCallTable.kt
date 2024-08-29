package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class representing the Argue the Call table on page 61 in the rulebook.
 */
object ArgueTheCallTable {
    private val table: Map<Int, ArgueTheCallResult> =
        mapOf(
            1 to ArgueTheCallResult.YOURE_OUTTA_HERE,
            2 to ArgueTheCallResult.I_DONT_CARE,
            3 to ArgueTheCallResult.I_DONT_CARE,
            4 to ArgueTheCallResult.I_DONT_CARE,
            5 to ArgueTheCallResult.I_DONT_CARE,
            6 to ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT,
        )

    /**
     * Roll on the Argue the Call table and return the result.
     */
    fun roll(
        d6: D6Result,
    ): ArgueTheCallResult {
        val result = d6.result
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Argue the Call Table.")
    }
}
