package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.CornerThrowInPosition
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.Serializable

@Serializable
class FieldSquare(val coordinates: FieldCoordinate) : Observable<FieldSquare>(), Location {
    constructor(x: Int, y: Int) : this(FieldCoordinate(x, y))

    override val coordinate: FieldCoordinate = coordinates
    val x = coordinates.x
    val y = coordinates.y
    var player: Player? = null // observable(null)
    var ball: Ball? = null // by observable(null)
    var hasTrapdoor: Boolean = false

    // Is field unoccupied as per the definition on page 44 in the rulebook.
    fun isUnoccupied(): Boolean = (player == null)

    // Is field occupied as per the definition on page 44 in the rulebook.
    fun isOccupied(): Boolean = !isUnoccupied()

    fun isOnTeamHalf(
        team: Team,
        rules: Rules,
    ): Boolean {
        return if (team.isHomeTeam()) isOnHomeSide(rules) else isOnAwaySide(rules)
    }

    val squareFlow: SharedFlow<FieldSquare> = observeState

    override fun isOnLineOfScrimmage(rules: Rules): Boolean = coordinates.isOnLineOfScrimmage(rules)

    override fun isInWideZone(rules: Rules): Boolean = coordinates.isInWideZone(rules)

    override fun isInEndZone(rules: Rules): Boolean = coordinates.isInEndZone(rules)

    override fun isInCenterField(rules: Rules): Boolean = coordinates.isInCenterField(rules)

    override fun isOnHomeSide(rules: Rules): Boolean = coordinates.isOnHomeSide(rules)

    override fun isOnAwaySide(rules: Rules): Boolean = coordinates.isOnAwaySide(rules)

    override fun isOnField(rules: Rules): Boolean = coordinates.isOnField(rules)

    override fun isOutOfBounds(rules: Rules): Boolean = false

    override fun getCornerLocation(rules: Rules): CornerThrowInPosition? = coordinates.getCornerLocation(rules)

    override fun isAdjacent(
        rules: Rules,
        location: Location,
    ): Boolean {
        return this.coordinates.distanceTo(location.coordinate) == 1u
    }
}
