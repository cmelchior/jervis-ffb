package com.jervisffb.engine.bb2020.tables

import com.jervisffb.engine.common.procedures.tables.prayers.BadHabits
import com.jervisffb.engine.common.procedures.tables.prayers.BlessedStatueOfNuffle
import com.jervisffb.engine.common.procedures.tables.prayers.FanInteraction
import com.jervisffb.engine.common.procedures.tables.prayers.FoulingFrenzy
import com.jervisffb.engine.common.procedures.tables.prayers.FriendsWithTheRef
import com.jervisffb.engine.common.procedures.tables.prayers.GreasyCleats
import com.jervisffb.engine.common.procedures.tables.prayers.IntensiveTraining
import com.jervisffb.engine.common.procedures.tables.prayers.IronMan
import com.jervisffb.engine.common.procedures.tables.prayers.KnuckleDusters
import com.jervisffb.engine.common.procedures.tables.prayers.MolesUnderThePitch
import com.jervisffb.engine.common.procedures.tables.prayers.NecessaryViolence
import com.jervisffb.engine.common.procedures.tables.prayers.PerfectPassing
import com.jervisffb.engine.common.procedures.tables.prayers.Stiletto
import com.jervisffb.engine.common.procedures.tables.prayers.ThrowARock
import com.jervisffb.engine.common.procedures.tables.prayers.TreacherousTrapdoor
import com.jervisffb.engine.common.procedures.tables.prayers.UnderScrutiny
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent

/**
 * List of all Prayer to Nuffle results in BB2020.

 * In many cases, the name is shared between BB2020 and BB2025, but the effect
 * is different, which is why they are tracked separately.
 *
 * See page 39 in the BB2020 rulebook.
 */
enum class BB2020PrayerToNuffleTableResult(
    override val description: String,
    override val procedure: Procedure,
    override val duration: Duration
): PrayerToNuffleEvent {
    TREACHEROUS_TRAPDOOR("Treacherous Trapdoor", TreacherousTrapdoor, Duration.END_OF_HALF),
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
