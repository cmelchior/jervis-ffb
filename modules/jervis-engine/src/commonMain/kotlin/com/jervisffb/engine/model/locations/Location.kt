package com.jervisffb.engine.model.locations

import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.CornerThrowInPosition

/**
 * Interface representing the abstract idea of a location. This can either
 * represent somewhere on or off the field, a players location or something
 * else that would have a place on a real Blood Bowl board.
 */
sealed interface Location {
    fun isOnLineOfScrimmage(rules: Rules): Boolean
    fun isInWideZone(rules: Rules): Boolean
    fun isInEndZone(rules: Rules): Boolean
    fun isInCenterField(rules: Rules): Boolean
    fun isOnHomeSide(rules: Rules): Boolean
    fun isOnAwaySide(rules: Rules): Boolean
    fun isOnField(rules: Rules): Boolean
    fun isOutOfBounds(rules: Rules): Boolean
    fun getCornerLocation(rules: Rules): CornerThrowInPosition?
    fun isAdjacent(rules: Rules, location: Location): Boolean
    fun overlap(otherLocation: Location): Boolean
}

