package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the standard Prayers To Nuffle Table.
 *
 * See page 143 in the BB 2025 rulebook.
 */
@Serializable
object StandardPrayersToNuffleTable: PrayersToNuffleTable {
    override val entries =
        mapOf(
            1 to PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR,
            2 to PrayerToNuffleTableResult2025.FRIENDS_WITH_THE_REF,
            3 to PrayerToNuffleTableResult2025.STILETTO,
            4 to PrayerToNuffleTableResult2025.IRON_MAN,
            5 to PrayerToNuffleTableResult2025.KNUCKLE_DUSTERS,
            6 to PrayerToNuffleTableResult2025.BAD_HABITS,
            7 to PrayerToNuffleTableResult2025.GREASY_CLEATS,
            8 to PrayerToNuffleTableResult2025.BLESSING_OF_NUFFLE,
            9 to PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH,
            10 to PrayerToNuffleTableResult2025.PERFECT_PASSING,
            11 to PrayerToNuffleTableResult2025.DAZZLING_CATCHING,
            12 to PrayerToNuffleTableResult2025.FAN_INTERACTION,
            13 to PrayerToNuffleTableResult2025.FOULING_FRENZY,
            14 to PrayerToNuffleTableResult2025.THROW_A_ROCK,
            15 to PrayerToNuffleTableResult2025.UNDER_SCRUTINY,
            16 to PrayerToNuffleTableResult2025.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffleTableResult2025 {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
