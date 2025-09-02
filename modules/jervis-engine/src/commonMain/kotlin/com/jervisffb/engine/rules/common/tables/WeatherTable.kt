package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.common.tables.Weather
import kotlinx.serialization.Serializable

/**
 * Interface representing a Weather Table.
 */
@Serializable
abstract class WeatherTable {
    abstract val name: String
    abstract fun roll(firstD6: D6Result, secondD6: D6Result): Weather

}
