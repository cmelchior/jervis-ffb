package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules

/**
 * Interface representing a location on the field of Blood Bowl match
 */
sealed interface OnFieldLocation: Location {

    /**
     * Move this location a number of steps in the given direction.
     * WARNING: This method does not check if the ball is still on the field, so
     * a coordinate outside the available field area can be returned.
     */
    fun move(
        direction: Direction,
        steps: Int,
    ): OnFieldLocation

    fun toLogString(): String

    /**
     * Return all on-field coordinates around a specific on-field location.
     * TODO Figure out if coordinates out of bounds should be returned as that or as their real coordinate
     * value (to make it easier to make calculations)
     */
    fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int = 1,
        includeOutOfBounds: Boolean = false,
    ): List<FieldCoordinate>

    /**
     * Returns the Chebyshev Distance between this field and the target location.
     * This is equal to the minimum number of squares between two squares on the game field.
     * For locations taking up multiple field squares, it is the squares closest to each
     * other that are used for the calculation
     *
     * See https://en.wikipedia.org/wiki/Chebyshev_distance
     */
    fun distanceTo(target: OnFieldLocation): Int

    /**
     * Returns the "real" distance between two fields, as if they were points in a coordinate system,
     * This means that unlike [distanceTo] diagonals will have a larger value than squares on a line.
     */
    fun realDistanceTo(target: OnFieldLocation): Double

    /**
     * Returns `true` if a field is diagonal to another, false if they are not.
     * This only works on two fields next to each other.
     */
    fun isDiagonalTo(target: OnFieldLocation): Boolean

    /**
     * Return all coordinates that are considered "away" from this coordinate from the point of view of the provided
     * [location].
     *
     * See page 45 in the rulebook.
     */
    fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: FieldCoordinate,
        includeOutOfBounds: Boolean = false,
    ): List<FieldCoordinate>
}
