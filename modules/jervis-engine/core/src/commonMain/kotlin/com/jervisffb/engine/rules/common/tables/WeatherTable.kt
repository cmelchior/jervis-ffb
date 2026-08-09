package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.actions.D6Result
import kotlinx.serialization.Serializable

/**
 * Interface representing a Weather Table.
 */
@Serializable
abstract class WeatherTable {
    abstract val name: String
    abstract val entries: Map<Int, Weather>
    abstract fun roll(firstD6: D6Result, secondD6: D6Result): Weather

}
