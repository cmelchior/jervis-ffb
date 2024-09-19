package dk.ilios.jervis.model.locations

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.CornerThrowInPosition
import kotlinx.serialization.Serializable

/**
 * Interface representing the abstract idea of a location, normally this is just
 * a square on the field, but it can also be other things, like a Dogout or
 * a Giants location (which can span multiple field squares).
 */
@Serializable
sealed interface Location {
    val coordinate: FieldCoordinate
    fun isOnLineOfScrimmage(rules: Rules): Boolean
    fun isInWideZone(rules: Rules): Boolean
    fun isInEndZone(rules: Rules): Boolean
    fun isInCenterField(rules: Rules): Boolean
    fun isOnHomeSide(rules: Rules): Boolean
    fun isOnAwaySide(rules: Rules): Boolean
    fun isOnField(rules: Rules): Boolean
    fun isOutOfBounds(rules: Rules): Boolean
    fun getCornerLocation(rules: Rules): CornerThrowInPosition?
    fun isAdjacent(
        rules: Rules,
        location: Location,
    ): Boolean
}

