package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.rules.common.tables.PrayerToNuffle
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the standard Prayers To Nuffle Table on page 39 in the rulebook.
 */
@Serializable
object BB2020StandardPrayersToNuffleTable: PrayersToNuffleTable {
    private val table =
        mapOf(
            1 to PrayerToNuffle.TREACHEROUS_TRAPDOOR,
            2 to PrayerToNuffle.FRIENDS_WITH_THE_REF,
            3 to PrayerToNuffle.STILETTO,
            4 to PrayerToNuffle.IRON_MAN,
            5 to PrayerToNuffle.KNUCKLE_DUSTERS,
            6 to PrayerToNuffle.BAD_HABITS,
            7 to PrayerToNuffle.GREASY_CLEATS,
            8 to PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE,
            9 to PrayerToNuffle.MOLES_UNDER_THE_PITCH,
            10 to PrayerToNuffle.PERFECT_PASSING,
            11 to PrayerToNuffle.FAN_INTERACTION,
            12 to PrayerToNuffle.NECESSARY_VIOLENCE,
            13 to PrayerToNuffle.FOULING_FRENZY,
            14 to PrayerToNuffle.THROW_A_ROCK,
            15 to PrayerToNuffle.UNDER_SCRUTINY,
            16 to PrayerToNuffle.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffle {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return table[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}

