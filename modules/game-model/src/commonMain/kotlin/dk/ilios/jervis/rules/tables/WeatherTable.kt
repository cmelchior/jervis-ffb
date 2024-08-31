package dk.ilios.jervis.rules.tables

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.utils.INVALID_GAME_STATE

sealed interface Weather {
    val title: String
}

data object SwelteringHeat : Weather {
    override val title: String = "Sweltering Heat"
}

data object VerySunny : Weather {
    override val title: String = "Very Sunny"
}

data object PerfectConditions : Weather {
    override val title: String = "Perfect conditions"
}

data object PouringRain : Weather {
    override val title: String = "Pouring Rain"
}

data object Blizzard : Weather {
    override val title: String = "Blizzard"
}

/**
 * Class representing the Weather Table on page 37 in the rulebook.
 */
object WeatherTable {
    private val table: Map<Int, Weather> =
        mapOf(
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
    fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
    ): Weather {
        val result = firstD6.value + secondD6.value
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Weather Table.")
    }
}
