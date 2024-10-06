package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.Serializable

@Serializable
class FieldSquare(
    val coordinates: FieldCoordinate
) : Observable<FieldSquare>(), FieldCoordinate by coordinates {
    constructor(x: Int, y: Int) : this(FieldCoordinate(x, y))
    var player: Player? = null
    // Having multiple balls in the same field should just be a temporary state
    // as the BB2020 Rules do not allow two balls in the same square.
    var balls: MutableList<Ball> = mutableListOf()
    var hasTrapdoor: Boolean = false

    // Is field unoccupied as per the definition on page 44 in the rulebook.
    fun isUnoccupied(): Boolean = (player == null)

    // Is field occupied as per the definition on page 44 in the rulebook.
    fun isOccupied(): Boolean = !isUnoccupied()

    fun isOnTeamHalf(team: Team, rules: Rules): Boolean {
        return if (team.isHomeTeam()) isOnHomeSide(rules) else isOnAwaySide(rules)
    }

    val squareFlow: SharedFlow<FieldSquare> = observeState
}
