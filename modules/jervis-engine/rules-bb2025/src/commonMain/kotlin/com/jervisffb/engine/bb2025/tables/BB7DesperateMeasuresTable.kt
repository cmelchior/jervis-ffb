package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Desperate Measures Table.
 *
 * See page 15 in Spike 22.
 */
@Serializable
object BB7DesperateMeasuresTable: DesperateMeasuresTable {
    override val entries =
        mapOf(
            1 to BB2025DesperateMeasuresTableResult.YOU_DOPE,
            2 to BB2025DesperateMeasuresTableResult.RAZZLE_DAZZLE,
            3 to BB2025DesperateMeasuresTableResult.HANGOVER,
            4 to BB2025DesperateMeasuresTableResult.GRUDGE_MATCH,
            5 to BB2025DesperateMeasuresTableResult.SET_PIECE,
            6 to BB2025DesperateMeasuresTableResult.SPORTS_ESPIONAGE,
            7 to BB2025DesperateMeasuresTableResult.DISCARDED_BANANA_SKIN,
            8 to BB2025DesperateMeasuresTableResult.MAGIC_SCROLL,
        )

    override fun roll(die: D8Result): DesperateMeasuresEvent {
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Desperate Measures table")
    }
}

