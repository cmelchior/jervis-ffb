package com.jervisffb.engine.model.locations

import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.CornerThrowInPosition

/**
 * Interface representing the abstract idea of a location on the main "pitch",
 * like a player position.
 *
 * It is also possible to represent locations outside the pitch but still using
* the pitch's coordinates system. In particular, this means that [DogOut] is
 * not represented this way.
 */
sealed interface Location {
    fun isOnLineOfScrimmage(rules: Rules): Boolean
    fun isInWideZone(rules: Rules): Boolean
    fun isInEndZone(rules: Rules): Boolean
    fun isInCenterField(rules: Rules): Boolean
    fun isInNoMansLand(rules: Rules): Boolean
    fun isOnHomeSide(rules: Rules): Boolean
    fun isOnAwaySide(rules: Rules): Boolean
    fun isOnPitch(rules: Rules): Boolean
    fun isOutOfBounds(rules: Rules): Boolean
    fun getCornerLocation(rules: Rules): CornerThrowInPosition?
    fun isAdjacent(rules: Rules, location: Location): Boolean
    // Returns true if this location overlaps with the otherLocation, i.e. some part of it
    // are the same position on the board.
    fun overlap(otherLocation: Location): Boolean
}

