package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.modifiers.StatModifier
import dk.ilios.jervis.model.modifiers.StatModifier.Type
import dk.ilios.jervis.procedures.tables.prayers.BadHabits
import dk.ilios.jervis.procedures.tables.prayers.BlessedStatueOfNuffle
import dk.ilios.jervis.procedures.tables.prayers.FanInteraction
import dk.ilios.jervis.procedures.tables.prayers.FoulingFrenzy
import dk.ilios.jervis.procedures.tables.prayers.FriendsWithTheRef
import dk.ilios.jervis.procedures.tables.prayers.GreasyCleats
import dk.ilios.jervis.procedures.tables.prayers.IntensiveTraining
import dk.ilios.jervis.procedures.tables.prayers.IronMan
import dk.ilios.jervis.procedures.tables.prayers.KnuckleDusters
import dk.ilios.jervis.procedures.tables.prayers.MolesUnderThePitch
import dk.ilios.jervis.procedures.tables.prayers.NecessaryViolence
import dk.ilios.jervis.procedures.tables.prayers.PerfectPassing
import dk.ilios.jervis.procedures.tables.prayers.Stiletto
import dk.ilios.jervis.procedures.tables.prayers.ThrowARock
import dk.ilios.jervis.procedures.tables.prayers.TreacherousTrapdoor
import dk.ilios.jervis.procedures.tables.prayers.UnderScrutiny
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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

enum class PrayerToNuffle(override val description: String, override val procedure: Procedure, override val duration: Duration): TableResult {
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
 * Class representing the Prayers To Nuffle Table on page 39 in the rulebook.
 */
object PrayersToNuffleTable {
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

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    fun roll(d16: D16Result): PrayerToNuffle {
        return table[d16.value] ?: INVALID_GAME_STATE("${d16.value} was not found in the Kick-Off Event Table.")
    }
}
