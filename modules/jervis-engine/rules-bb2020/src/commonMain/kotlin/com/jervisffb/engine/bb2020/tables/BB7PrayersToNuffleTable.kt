package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable
import com.jervisffb.engine.bb2020.tables.BB2020PrayerToNuffleTableResult as CommonPrayerToNuffleTableResult

/**
 * Class representing the BB7 Prayers To Nuffle Table on page 93 in the Death Zone rulebook.
 */
@Serializable
object BB7PrayersToNuffleTable: PrayersToNuffleTable {
    override val entries =
        mapOf(
            1 to CommonPrayerToNuffleTableResult.TREACHEROUS_TRAPDOOR,
            2 to CommonPrayerToNuffleTableResult.FRIENDS_WITH_THE_REF,
            3 to CommonPrayerToNuffleTableResult.STILETTO,
            4 to CommonPrayerToNuffleTableResult.IRON_MAN,
            5 to CommonPrayerToNuffleTableResult.KNUCKLE_DUSTERS,
            6 to CommonPrayerToNuffleTableResult.BAD_HABITS,
            7 to CommonPrayerToNuffleTableResult.GREASY_CLEATS,
            8 to CommonPrayerToNuffleTableResult.BLESSED_STATUE_OF_NUFFLE,
        )

    override val die: Dice = Dice.D8

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): CommonPrayerToNuffleTableResult {
        if (die !is D8Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
