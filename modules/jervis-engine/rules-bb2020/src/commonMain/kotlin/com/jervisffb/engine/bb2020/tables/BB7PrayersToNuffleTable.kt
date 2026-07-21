package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.common.tables.PrayerToNuffleTableResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the BB7 Prayers To Nuffle Table on page 93 in the Death Zone rulebook.
 */
@Serializable
object BB7PrayersToNuffleTable: PrayersToNuffleTable {
    private val table =
        mapOf(
            1 to PrayerToNuffleTableResult.TREACHEROUS_TRAPDOOR,
            2 to PrayerToNuffleTableResult.FRIENDS_WITH_THE_REF,
            3 to PrayerToNuffleTableResult.STILETTO,
            4 to PrayerToNuffleTableResult.IRON_MAN,
            5 to PrayerToNuffleTableResult.KNUCKLE_DUSTERS,
            6 to PrayerToNuffleTableResult.BAD_HABITS,
            7 to PrayerToNuffleTableResult.GREASY_CLEATS,
            8 to PrayerToNuffleTableResult.BLESSED_STATUE_OF_NUFFLE,
        )

    override val die: Dice = Dice.D8

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffleTableResult {
        if (die !is D8Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return table[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
