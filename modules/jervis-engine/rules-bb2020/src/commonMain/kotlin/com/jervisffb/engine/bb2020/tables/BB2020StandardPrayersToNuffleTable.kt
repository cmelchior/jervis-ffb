package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleTableResult
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the standard Prayers To Nuffle Table.
 *
 * See page 39 in the BB2020 rulebook.
 */
@Serializable
object BB2020StandardPrayersToNuffleTable: PrayersToNuffleTable {
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
            9 to PrayerToNuffleTableResult.MOLES_UNDER_THE_PITCH,
            10 to PrayerToNuffleTableResult.PERFECT_PASSING,
            11 to PrayerToNuffleTableResult.FAN_INTERACTION,
            12 to PrayerToNuffleTableResult.NECESSARY_VIOLENCE,
            13 to PrayerToNuffleTableResult.FOULING_FRENZY,
            14 to PrayerToNuffleTableResult.THROW_A_ROCK,
            15 to PrayerToNuffleTableResult.UNDER_SCRUTINY,
            16 to PrayerToNuffleTableResult.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffleTableResult {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return table[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}

