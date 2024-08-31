package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D16Result
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
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Class representing the Prayers To Nuffle Table on page 39 in the rulebook.
 */
object PrayersToNuffleTable {
    private val table =
        mapOf(
            1 to TableResult("Treacherous Trapdor", TreacherousTrapdoor),
            2 to TableResult("Friends with the Ref", FriendsWithTheRef),
            3 to TableResult("Stiletto", Stiletto),
            4 to TableResult("Iron Man", IronMan),
            5 to TableResult("Knuckle Dusters", KnuckleDusters),
            6 to TableResult("Bad Habits", BadHabits),
            7 to TableResult("Greasy Cleats", GreasyCleats),
            8 to TableResult("Blessed Statue of Nuffle", BlessedStatueOfNuffle),
            9 to TableResult("Moles under the Pitch", MolesUnderThePitch),
            10 to TableResult("Perfect Passing", PerfectPassing),
            11 to TableResult("Fan Interaction", FanInteraction),
            12 to TableResult("Necessary Violence", NecessaryViolence),
            13 to TableResult("Fouling Frenzy", FoulingFrenzy),
            14 to TableResult("Throw a Rock", ThrowARock),
            15 to TableResult("Under Scrutiny", UnderScrutiny),
            16 to TableResult("Intensive Training", IntensiveTraining),
        )

    /**
     * Roll on the Prayers of Nuffle table and return the result.
     */
    fun roll(d16: D16Result): TableResult {
        return table[d16.value] ?: INVALID_GAME_STATE("${d16.value} was not found in the Kick-Off Event Table.")
    }
}
