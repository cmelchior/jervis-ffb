package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the BB7 Prayers To Nuffle Table on page 15 in Spike 22.
 */
@Serializable
object BB7PrayersToNuffleTable: PrayersToNuffleTable {
    override val entries =
        mapOf(
            1 to PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR,
            2 to PrayerToNuffleTableResult2025.STILETTO,
            3 to PrayerToNuffleTableResult2025.IRON_MAN,
            4 to PrayerToNuffleTableResult2025.KNUCKLE_DUSTERS,
            5 to PrayerToNuffleTableResult2025.BLESSING_OF_NUFFLE,
            6 to PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH,
            7 to PrayerToNuffleTableResult2025.UNDER_SCRUTINY,
            8 to PrayerToNuffleTableResult2025.INTENSIVE_TRAINING,
        )

    override val die: Dice = Dice.D8

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffleTableResult2025 {
        if (die !is D8Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
