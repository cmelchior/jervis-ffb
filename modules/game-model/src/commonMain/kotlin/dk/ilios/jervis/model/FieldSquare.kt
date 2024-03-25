package dk.ilios.jervis.model

import dk.ilios.jervis.rules.Rules
import kotlinx.coroutines.flow.SharedFlow

class FieldSquare(val coordinates: FieldCoordinate): Observable<FieldSquare>(), Location {
    constructor(x: Int, y: Int): this(FieldCoordinate(x, y))
    override val coordinate: FieldCoordinate = coordinates
    val x = coordinates.x
    val y = coordinates.y
    var player: Player? by observable(null)
    var ball: Ball? by observable(null)

    fun isEmpty(): Boolean = (player == null)
    fun isNotEmpty(): Boolean = !isEmpty()
    fun isOnTeamHalf(team: Team, rules: Rules): Boolean {
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
    override fun isAdjacent(rules: Rules, location: Location): Boolean {
        return this.coordinates.distanceTo(location.coordinate) == 1u
    }
}