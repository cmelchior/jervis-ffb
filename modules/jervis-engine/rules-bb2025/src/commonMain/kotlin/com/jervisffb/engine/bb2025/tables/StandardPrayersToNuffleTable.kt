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
            1 to BB2025PrayerToNuffleTableResult.TREACHEROUS_TRAPDOOR,
            2 to BB2025PrayerToNuffleTableResult.FRIENDS_WITH_THE_REF,
            3 to BB2025PrayerToNuffleTableResult.STILETTO,
            4 to BB2025PrayerToNuffleTableResult.IRON_MAN,
            5 to BB2025PrayerToNuffleTableResult.KNUCKLE_DUSTERS,
            6 to BB2025PrayerToNuffleTableResult.BAD_HABITS,
            7 to BB2025PrayerToNuffleTableResult.GREASY_CLEATS,
            8 to BB2025PrayerToNuffleTableResult.BLESSING_OF_NUFFLE,
            9 to BB2025PrayerToNuffleTableResult.MOLES_UNDER_THE_PITCH,
            10 to BB2025PrayerToNuffleTableResult.PERFECT_PASSING,
            11 to BB2025PrayerToNuffleTableResult.DAZZLING_CATCHING,
            12 to BB2025PrayerToNuffleTableResult.FAN_INTERACTION,
            13 to BB2025PrayerToNuffleTableResult.FOULING_FRENZY,
            14 to BB2025PrayerToNuffleTableResult.THROW_A_ROCK,
            15 to BB2025PrayerToNuffleTableResult.UNDER_SCRUTINY,
            16 to BB2025PrayerToNuffleTableResult.INTENSIVE_TRAINING
        )

    override val die: Dice = Dice.D16

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): BB2025PrayerToNuffleTableResult {
        if (die !is D16Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return entries[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
