package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.model.modifiers.StatModifier.Type
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.BlessedStatueOfNuffle
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.FanInteraction
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.FoulingFrenzy
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.FriendsWithTheRef
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.GreasyCleats
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.IntensiveTraining
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.IronMan
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.KnuckleDusters
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.MolesUnderThePitch
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.NecessaryViolence
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.PerfectPassing
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.Stiletto
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.ThrowARock
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.TreacherousTrapdoor
import com.jervisffb.engine.rules.bb2020.procedures.tables.prayers.UnderScrutiny
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

// Consider: Do we really need this enum?
enum class PrayerStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: Type,
    override val expiresAt: Duration
): StatModifier {
    IRON_MAN("Iron Man", 1, Type.AV, Duration.END_OF_GAME),
    GREASY_CLEATS("Greasy Cleats", -1, Type.MA, Duration.END_OF_DRIVE),
}

enum class PrayerToNuffle(override val description: String, override val procedure: Procedure, override val duration: Duration):
    TableResult {
    TREACHEROUS_TRAPDOOR("Treacherous Trapdor", TreacherousTrapdoor, Duration.END_OF_HALF),
    FRIENDS_WITH_THE_REF("Friends with the Ref", FriendsWithTheRef, Duration.END_OF_DRIVE),
    STILETTO("Stiletto", Stiletto, Duration.END_OF_DRIVE),
    IRON_MAN("Iron Man", IronMan, Duration.END_OF_DRIVE),
    KNUCKLE_DUSTERS("Knuckle Dusters", KnuckleDusters, Duration.END_OF_DRIVE),
    BAD_HABITS("Bad Habits", BadHabits, Duration.END_OF_DRIVE),
    GREASY_CLEATS("Greasy Cleats", GreasyCleats, Duration.END_OF_DRIVE),
    BLESSED_STATUE_OF_NUFFLE("Blessed Statue of Nuffle", BlessedStatueOfNuffle, Duration.END_OF_GAME),
    MOLES_UNDER_THE_PITCH("Moles under the Pitch", MolesUnderThePitch, Duration.END_OF_HALF),
    PERFECT_PASSING("Perfect Passing", PerfectPassing, Duration.END_OF_GAME),
    FAN_INTERACTION("Fan Interaction", FanInteraction, Duration.END_OF_DRIVE),
    NECESSARY_VIOLENCE("Necessary Violence", NecessaryViolence, Duration.END_OF_DRIVE),
    FOULING_FRENZY("Fouling Frenzy", FoulingFrenzy, Duration.END_OF_DRIVE),
    THROW_A_ROCK("Throw a Rock", ThrowARock, Duration.END_OF_DRIVE),
    UNDER_SCRUTINY("Under Scrutiny", UnderScrutiny, Duration.END_OF_HALF),
    INTENSIVE_TRAINING("Intensive Training", IntensiveTraining, Duration.END_OF_GAME),
}

/**
 * Interface representing rolling on the Prayers To Nuffle Table.
 *
 * @param D which type of die to roll
 */
interface PrayersToNuffleTable {
    val die: Dice // Which die is used to roll the table
    /**
     * Roll on the table. Should throw an [com.jervisffb.engine.utils.INVALID_ACTION]
     * exception if the die does not match [die].
     */
    fun roll(die: DieResult): PrayerToNuffle
}

/**
 * Class representing the standard Prayers To Nuffle Table on page 39 in the rulebook.
 */
@Serializable
object StandardPrayersToNuffleTable: PrayersToNuffleTable {
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

/**
 * Class representing the standard Prayers To Nuffle Table on page 93 in the Death Zone rulebook.
 */
@Serializable
object BB7PrayersToNuffleTable: PrayersToNuffleTable {
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
        )

    override val die: Dice = Dice.D8

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    override fun roll(die: DieResult): PrayerToNuffle {
        if (die !is D8Result) INVALID_ACTION(die, "Wrong die type: ${die::class}")
        return table[die.value] ?: INVALID_GAME_STATE("${die.value} was not found in the Prayers To Nuffle table")
    }
}
