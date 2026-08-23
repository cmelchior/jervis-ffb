package com.jervisffb.engine.bb2025.tables

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
import com.jervisffb.engine.common.procedures.tables.prayers.PerfectPassing
import com.jervisffb.engine.common.procedures.tables.prayers.Stiletto
import com.jervisffb.engine.common.procedures.tables.prayers.ThrowARock
import com.jervisffb.engine.common.procedures.tables.prayers.TreacherousTrapdoor
import com.jervisffb.engine.common.procedures.tables.prayers.UnderScrutiny
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.tables.PrayerToNuffleEvent

/**
 * List of all Prayer to Nuffle results in BB2025.
 * In many cases, the name is shared between BB2020 and BB2025, but the effect
 * is different, which is why they are tracked separately.
 *
 * See page 143 in the BB2025 rulebook.
 */
enum class BB2025PrayerToNuffleTableResult(
    override val description: String,
    override val procedure: Procedure,
    override val duration: Duration
): PrayerToNuffleEvent {
    TREACHEROUS_TRAPDOOR("Treacherous Trapdoor", TreacherousTrapdoor, Duration.END_OF_GAME),
    FRIENDS_WITH_THE_REF("Friends with the Ref", FriendsWithTheRef, Duration.END_OF_GAME),
    STILETTO("Stiletto", Stiletto, Duration.END_OF_GAME),
    IRON_MAN("Iron Man", IronMan, Duration.END_OF_GAME),
    KNUCKLE_DUSTERS("Knuckle Dusters", KnuckleDusters, Duration.END_OF_GAME),
    BAD_HABITS("Bad Habits", BadHabits, Duration.END_OF_GAME),
    GREASY_CLEATS("Greasy Cleats", GreasyCleats, Duration.END_OF_GAME),
    BLESSING_OF_NUFFLE("Blessing of Nuffle", BlessedStatueOfNuffle, Duration.END_OF_GAME),
    MOLES_UNDER_THE_PITCH("Moles under the Pitch", MolesUnderThePitch, Duration.END_OF_GAME),
    PERFECT_PASSING("Perfect Passing", PerfectPassing, Duration.END_OF_GAME),
    DAZZLING_CATCHING("Dazzling Catching", DummyProcedure, Duration.END_OF_GAME),
    FAN_INTERACTION("Fan Interaction", FanInteraction, Duration.END_OF_GAME),
    FOULING_FRENZY("Fouling Frenzy", FoulingFrenzy, Duration.END_OF_GAME),
    THROW_A_ROCK("Throw a Rock", ThrowARock, Duration.END_OF_GAME),
    UNDER_SCRUTINY("Under Scrutiny", UnderScrutiny, Duration.END_OF_GAME),
    INTENSIVE_TRAINING("Intensive Training", IntensiveTraining, Duration.END_OF_GAME),
}
