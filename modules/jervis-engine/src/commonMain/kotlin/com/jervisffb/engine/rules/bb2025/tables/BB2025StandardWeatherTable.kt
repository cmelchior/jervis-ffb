package com.jervisffb.engine.rules.bb2025.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.rules.common.tables.WeatherTable
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

/**
 * Class representing the Weather Table.
 *
 * See page XX in the BB 2025 rulebook.
 */
@Serializable
object BB2025StandardWeatherTable: WeatherTable() {
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
