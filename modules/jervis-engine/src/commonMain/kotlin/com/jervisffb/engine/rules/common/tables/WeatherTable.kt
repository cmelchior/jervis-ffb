package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.Weather

/**
 * Interface representing a Weather Table.
 */
interface WeatherTable {
    val name: String
    fun roll(firstD6: D6Result, secondD6: D6Result): Weather
}
