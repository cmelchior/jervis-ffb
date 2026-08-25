package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable
import com.jervisffb.engine.bb2020.tables.PrayerToNuffleTableResult2020 as PrayerToNuffleTableResultCommon

/**
 * Class representing the standard Prayers To Nuffle Table.
 *
 * See page 39 in the BB2020 rulebook.
 */
@Serializable
object StandardPrayersToNuffleTable2020: PrayersToNuffleTable {
    override val entries =
        mapOf(
            1 to PrayerToNuffleTableResultCommon.TREACHEROUS_TRAPDOOR,
            2 to PrayerToNuffleTableResultCommon.FRIENDS_WITH_THE_REF,
            3 to PrayerToNuffleTableResultCommon.STILETTO,
            4 to PrayerToNuffleTableResultCommon.IRON_MAN,
            5 to PrayerToNuffleTableResultCommon.KNUCKLE_DUSTERS,
            6 to PrayerToNuffleTableResultCommon.BAD_HABITS,
            7 to PrayerToNuffleTableResultCommon.GREASY_CLEATS,
            8 to PrayerToNuffleTableResultCommon.BLESSED_STATUE_OF_NUFFLE,
            9 to PrayerToNuffleTableResultCommon.MOLES_UNDER_THE_PITCH,
            10 to PrayerToNuffleTableResultCommon.PERFECT_PASSING,
            11 to PrayerToNuffleTableResultCommon.FAN_INTERACTION,
            12 to PrayerToNuffleTableResultCommon.NECESSARY_VIOLENCE,
            13 to PrayerToNuffleTableResultCommon.FOULING_FRENZY,
            14 to PrayerToNuffleTableResultCommon.THROW_A_ROCK,
            15 to PrayerToNuffleTableResultCommon.UNDER_SCRUTINY,
            16 to PrayerToNuffleTableResultCommon.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffleTableResultCommon {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
