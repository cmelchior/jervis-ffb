package com.jervisffb.engine.model.locations

import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.tables.CornerThrowInPosition

data object DogOut : Location {
//    override val coordinate: FieldCoordinate = FieldCoordinate.UNKNOWN
    override fun isOnLineOfScrimmage(rules: Rules): Boolean = false
    override fun isInWideZone(rules: Rules): Boolean = false
    override fun isInEndZone(rules: Rules): Boolean = false
    override fun isInCenterField(rules: Rules): Boolean = false
    override fun isInNoMansLand(rules: Rules): Boolean = false
    override fun isOnHomeSide(rules: Rules): Boolean = false
    override fun isOnAwaySide(rules: Rules): Boolean = false
    override fun isOnField(rules: Rules): Boolean = false
    override fun isOutOfBounds(rules: Rules): Boolean = false
    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? = null
    override fun isAdjacent(rules: Rules, location: Location): Boolean = false
    override fun overlap(otherLocation: Location): Boolean = otherLocation == DogOut
}
