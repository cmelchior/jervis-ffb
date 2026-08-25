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
            1 to DesperateMeasuresTableResult2025.YOU_DOPE,
            2 to DesperateMeasuresTableResult2025.RAZZLE_DAZZLE,
            3 to DesperateMeasuresTableResult2025.HANGOVER,
            4 to DesperateMeasuresTableResult2025.GRUDGE_MATCH,
            5 to DesperateMeasuresTableResult2025.SET_PIECE,
            6 to DesperateMeasuresTableResult2025.SPORTS_ESPIONAGE,
            7 to DesperateMeasuresTableResult2025.DISCARDED_BANANA_SKIN,
            8 to DesperateMeasuresTableResult2025.MAGIC_SCROLL,
        )

    override fun roll(die: D8Result): DesperateMeasuresEvent {
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Desperate Measures table")
    }
}

