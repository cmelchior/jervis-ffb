package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.modifiers.StatModifier
import dk.ilios.jervis.model.modifiers.StatModifier.Type
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.BadHabits
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.BlessedStatueOfNuffle
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.FanInteraction
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.FoulingFrenzy
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.FriendsWithTheRef
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.GreasyCleats
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.IntensiveTraining
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.IronMan
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.KnuckleDusters
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.MolesUnderThePitch
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.NecessaryViolence
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.PerfectPassing
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.Stiletto
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.ThrowARock
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.TreacherousTrapdoor
import dk.ilios.jervis.procedures.bb2020.prayersofnuffle.UnderScrutiny
import dk.ilios.jervis.rules.skills.ResetPolicy
import dk.ilios.jervis.utils.INVALID_GAME_STATE

// Consider: Do we really need this enum?
enum class PrayerStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: Type,
    override val expiresAt: ResetPolicy
): StatModifier {
    IRON_MAN("Iron Man", 1, Type.AV, ResetPolicy.END_OF_GAME),
    GREASY_CLEATS("Greasy Cleats", -1, Type.MA, ResetPolicy.END_OF_DRIVE),
}

enum class PrayerToNuffle(override val description: String, override val procedure: Procedure, override val duration: ResetPolicy): TableResult {
    TREACHEROUS_TRAPDOOR("Treacherous Trapdor", TreacherousTrapdoor, ResetPolicy.END_OF_HALF),
    FRIENDS_WITH_THE_REF("Friends with the Ref", FriendsWithTheRef, ResetPolicy.END_OF_DRIVE),
    STILETTO("Stiletto", Stiletto, ResetPolicy.END_OF_DRIVE),
    IRON_MAN("Iron Man", IronMan, ResetPolicy.END_OF_DRIVE),
    KNUCKLE_DUSTERS("Knuckle Dusters", KnuckleDusters, ResetPolicy.END_OF_DRIVE),
    BAD_HABITS("Bad Habits", BadHabits, ResetPolicy.END_OF_DRIVE),
    GREASY_CLEATS("Greasy Cleats", GreasyCleats, ResetPolicy.END_OF_DRIVE),
    BLESSED_STATUE_OF_NUFFLE("Blessed Statue of Nuffle", BlessedStatueOfNuffle, ResetPolicy.END_OF_GAME),
    MOLES_UNDER_THE_PITCH("Moles under the Pitch", MolesUnderThePitch, ResetPolicy.END_OF_HALF),
    PERFECT_PASSING("Perfect Passing", PerfectPassing, ResetPolicy.END_OF_GAME),
    FAN_INTERACTION("Fan Interaction", FanInteraction, ResetPolicy.END_OF_DRIVE),
    NECESSARY_VIOLENCE("Necessary Violence", NecessaryViolence, ResetPolicy.END_OF_DRIVE),
    FOULING_FRENZY("Fouling Frenzy", FoulingFrenzy, ResetPolicy.END_OF_DRIVE),
    THROW_A_ROCK("Throw a Rock", ThrowARock, ResetPolicy.END_OF_DRIVE),
    UNDER_SCRUTINY("Under Scrutiny", UnderScrutiny, ResetPolicy.END_OF_HALF),
    INTENSIVE_TRAINING("Intensive Training", IntensiveTraining, ResetPolicy.END_OF_GAME),
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
