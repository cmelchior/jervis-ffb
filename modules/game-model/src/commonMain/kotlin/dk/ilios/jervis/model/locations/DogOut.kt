package dk.ilios.jervis.model.locations

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.CornerThrowInPosition

data object DogOut : Location {
//    override val coordinate: FieldCoordinate = FieldCoordinate.UNKNOWN
    override fun isOnLineOfScrimmage(rules: Rules): Boolean = false
    override fun isInWideZone(rules: Rules): Boolean = false
    override fun isInEndZone(rules: Rules): Boolean = false
    override fun isInCenterField(rules: Rules): Boolean = false
    override fun isOnHomeSide(rules: Rules): Boolean = false
    override fun isOnAwaySide(rules: Rules): Boolean = false
    override fun isOnField(rules: Rules): Boolean = false
    override fun isOutOfBounds(rules: Rules): Boolean = false
    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? = null
    override fun isAdjacent(rules: Rules, location: Location): Boolean = false
}
