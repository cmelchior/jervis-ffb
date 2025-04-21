package com.jervisffb.engine.rules.bb2020.tables

import com.jervisffb.engine.actions.D6Result

/**
 * Interface representing a Weather Table.
 */
interface WeatherTable {
    val name: String
    fun roll(firstD6: D6Result, secondD6: D6Result): Weather
}
