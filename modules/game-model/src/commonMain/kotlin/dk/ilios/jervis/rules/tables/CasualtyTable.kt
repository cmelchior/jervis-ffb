package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class representing the Injury Table on page 60 in the rulebook.
 */
object CasualtyTable {
    private val table: Map<Int, CasualtyResult> =
        mapOf(
            1 to CasualtyResult.BADLY_HURT,
            2 to CasualtyResult.BADLY_HURT,
            3 to CasualtyResult.BADLY_HURT,
            4 to CasualtyResult.BADLY_HURT,
            5 to CasualtyResult.BADLY_HURT,
            6 to CasualtyResult.BADLY_HURT,
            7 to CasualtyResult.SERIOUS_HURT,
            8 to CasualtyResult.SERIOUS_HURT,
            9 to CasualtyResult.SERIOUS_HURT,
            10 to CasualtyResult.SERIOUS_INJURY,
            11 to CasualtyResult.SERIOUS_INJURY,
            12 to CasualtyResult.SERIOUS_INJURY,
            13 to CasualtyResult.LASTING_INJURY,
            14 to CasualtyResult.LASTING_INJURY,
            15 to CasualtyResult.DEAD,
            16 to CasualtyResult.DEAD
        )

    /**
     * Roll on the Injury table and return the result.
     */
    fun roll(
        d16: D16Result,
    ): CasualtyResult {
        val result = d16.result
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Casulty Table.")
    }
}
