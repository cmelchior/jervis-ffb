package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class representing the Stunty Injury Table on page 60 in the rulebook.
 */
object StuntyInjuryTable {
    private val table: Map<Int, InjuryResult> =
        mapOf(
            2 to InjuryResult.STUNNED,
            3 to InjuryResult.STUNNED,
            4 to InjuryResult.STUNNED,
            5 to InjuryResult.STUNNED,
            6 to InjuryResult.STUNNED,
            7 to InjuryResult.KO,
            8 to InjuryResult.KO,
            9 to InjuryResult.BADLY_HURT,
            10 to InjuryResult.CASUALTY,
            11 to InjuryResult.CASUALTY,
            12 to InjuryResult.CASUALTY,
        )

    /**
     * Roll on the Stunty Injury table and return the result.
     */
    fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
    ): InjuryResult {
        val result = firstD6.result + secondD6.result
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Stunty Injury Table.")
    }
}
