package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable
import com.jervisffb.engine.bb2020.tables.BB2020PrayerToNuffleTableResult as CommonPrayerToNuffleTableResult

/**
 * Class representing the standard Prayers To Nuffle Table.
 *
 * See page 39 in the BB2020 rulebook.
 */
@Serializable
object BB2020StandardPrayersToNuffleTable: PrayersToNuffleTable {
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
            9 to CommonPrayerToNuffleTableResult.MOLES_UNDER_THE_PITCH,
            10 to CommonPrayerToNuffleTableResult.PERFECT_PASSING,
            11 to CommonPrayerToNuffleTableResult.FAN_INTERACTION,
            12 to CommonPrayerToNuffleTableResult.NECESSARY_VIOLENCE,
            13 to CommonPrayerToNuffleTableResult.FOULING_FRENZY,
            14 to CommonPrayerToNuffleTableResult.THROW_A_ROCK,
            15 to CommonPrayerToNuffleTableResult.UNDER_SCRUTINY,
            16 to CommonPrayerToNuffleTableResult.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): CommonPrayerToNuffleTableResult {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
