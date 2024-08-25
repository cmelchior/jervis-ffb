package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class representing the Injury Table on page 60 in the rulebook.
 */
object InjuryTable {
    private val table: Map<Int, InjuryResult> =
        mapOf(
            2 to InjuryResult.STUNNED,
            3 to InjuryResult.STUNNED,
            4 to InjuryResult.STUNNED,
            5 to InjuryResult.STUNNED,
            6 to InjuryResult.STUNNED,
            7 to InjuryResult.STUNNED,
            8 to InjuryResult.KO,
            9 to InjuryResult.KO,
            10 to InjuryResult.CASUALTY,
            11 to InjuryResult.CASUALTY,
            12 to InjuryResult.CASUALTY,
        )

    /**
     * Roll on the Injury table and return the result.
     */
    fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
        modifier: Int = 0,
    ): InjuryResult {
        val result = (firstD6.result + secondD6.result + modifier).coerceIn(2, 12)
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Injury Table.")
    }
}
