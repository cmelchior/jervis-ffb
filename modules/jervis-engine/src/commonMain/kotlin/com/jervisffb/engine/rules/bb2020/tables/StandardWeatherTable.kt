package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Interface representing a Weather Table.
 */
interface WeatherTable {
    val name: String
    fun roll(firstD6: D6Result, secondD6: D6Result): Weather
}

/**
 * Class representing the Weather Table on page 37 in the rulebook.
 */
object StandardWeatherTable: WeatherTable {
    override val name: String = "Standard Weather Table"
    private val table: Map<Int, Weather> =
        mapOf(
            2 to Weather.SWELTERING_HEAT,
            3 to Weather.VERY_SUNNY,
            4 to Weather.PERFECT_CONDITIONS,
            5 to Weather.PERFECT_CONDITIONS,
            6 to Weather.PERFECT_CONDITIONS,
            7 to Weather.PERFECT_CONDITIONS,
            8 to Weather.PERFECT_CONDITIONS,
            9 to Weather.PERFECT_CONDITIONS,
            10 to Weather.PERFECT_CONDITIONS,
            11 to Weather.POURING_RAIN,
            12 to Weather.BLIZZARD,
        )

    /**
     * Roll on the Weather table and return the result.
     */
    override fun roll(
        firstD6: D6Result,
        secondD6: D6Result,
    ): Weather {
        val result = firstD6.value + secondD6.value
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Weather Table.")
    }
}

/**
 * Class representing the Spring Weather Table on page 76 in the Death Zone rulebook.
 */
object SpringWeatherTable: WeatherTable {
    override val name: String = "Spring Weather Table"
    override fun roll(firstD6: D6Result, secondD6: D6Result): Weather {
        TODO("Not yet implemented")
    }
}

/**
 * Class representing the Summer Weather Table on page 77 in the Death Zone rulebook.
 */
object SummerWeatherTable: WeatherTable {
    override val name: String = "Summer Weather Table"
    override fun roll(firstD6: D6Result, secondD6: D6Result): Weather {
        TODO("Not yet implemented")
    }
}

/**
 * Class representing the Autumn Weather Table on page 77 in the Death Zone rulebook.
 */
object AutumnWeatherTable: WeatherTable {
    override val name: String = "Autumn Weather Table"
    override fun roll(firstD6: D6Result, secondD6: D6Result): Weather {
        TODO("Not yet implemented")
    }
}

/**
 * Class representing the Winter Weather Table on page 78 in the Death Zone rulebook.
 */
object WinterWeatherTable: WeatherTable {
    override val name: String
        get() = TODO("Not yet implemented")

    override fun roll(firstD6: D6Result, secondD6: D6Result): Weather {
        TODO("Not yet implemented")
    }
}
