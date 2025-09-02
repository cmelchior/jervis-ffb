package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Injury Table on page 60 in the rulebook.
 */
@Serializable
object BB2020StandardInjuryTable: InjuryTable() {
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

    override fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
        modifier: Int,
    ): InjuryResult {
        val result = rollDices(firstD6, secondD6, modifier)
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Injury Table.")
    }
}
