package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D6Result
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

sealed interface Weather
data object SwelteringHeat: Weather
data object VerySunny: Weather
data object PerfectConditions: Weather
data object PouringRain: Weather
data object Blizzard: Weather

/**
 * Class representing the Weather Table on page 37 in the rulebook.
 */
object WeatherTable {
    private val table: Map<Int, Weather> = mapOf(
        2 to SwelteringHeat,
        3 to VerySunny,
        4 to PerfectConditions,
        5 to PerfectConditions,
        6 to PerfectConditions,
        7 to PerfectConditions,
        8 to PerfectConditions,
        9 to PerfectConditions,
        10 to PerfectConditions,
        11 to PouringRain,
        12 to Blizzard,
    )

    /**
     * Roll on the Weather table and return the result.
     */
    fun roll(firstD6: D6Result, secondD6: D6Result): Weather {
        val result = firstD6.result + secondD6.result
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Weather Table.")
    }
}
