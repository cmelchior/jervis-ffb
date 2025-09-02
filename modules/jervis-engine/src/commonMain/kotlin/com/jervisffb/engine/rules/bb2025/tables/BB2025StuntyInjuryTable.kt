package com.jervisffb.engine.rules.bb2025.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Stunty Injury Table.
 *
 * See page XX in the BB 2025 rulebook.
 */
@Serializable
object BB2025StuntyInjuryTable: InjuryTable() {
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
    override fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
        modifier: Int
    ): InjuryResult {
        val result = rollDices(firstD6, secondD6, modifier)
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Injury Table.")
    }
}
