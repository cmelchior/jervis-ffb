package com.jervisffb.engine.model.locations

import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.Rules

/**
 * Interface representing a location on the pitch of a Blood Bowl match.
 * This does not include the [Dogout].
 */
sealed interface OnPitchLocation: Location {

    /**
     * Move this location a number of steps in the given direction.
     * WARNING: This method does not check if the location is still on the pitch, so
     * a coordinate outside the available pitch area can be returned.
     */
    fun move(
        direction: Direction,
        steps: Int,
    ): OnPitchLocation

    /**
     * Returns a nice representation of this location that can be used
     * in the log output.
     */
    fun toLogString(): String

    /**
     * Return all on-pitch coordinates around a specific on-pitch location.
     * TODO Figure out if coordinates out of bounds should be returned as that
     * or as their real coordinate value (to make it easier to make calculations)
     */
    fun getSurroundingCoordinates(
        rules: Rules,
        distance: Int = 1,
        includeOutOfBounds: Boolean = false,
    ): List<PitchCoordinate>

    /**
     * Returns the Chebyshev Distance between this location and the target location.
     * This is equal to the minimum number of squares between two locations on the game pitch.
     * For locations taking up multiple pitch squares, it is the squares closest to each
     * other that are used for the calculation.
     *
     * See https://en.wikipedia.org/wiki/Chebyshev_distance
     */
    fun distanceTo(target: OnPitchLocation): Int

    /**
     * Returns the "real" minimum distance between two locations, as if they were points in a coordinate system,
     * This means that unlike [distanceTo] diagonals will have a larger value than squares on a line.
     */
    fun realDistanceTo(target: OnPitchLocation): Double

    /**
     * Returns `true` if a location is diagonal to another, false if they are not.
     * If two locations are not adjacent to each other, `false` is returned.
     */
    fun isDiagonalTo(target: OnPitchLocation): Boolean

    /**
     * Return all coordinates that are considered "away" from this coordinate from the point of view of the provided
     * [location].
     *
     * See page 45 in the BB2025 rulebook.
     */
    fun getCoordinatesAwayFromLocation(
        rules: Rules,
        location: PitchCoordinate,
        includeOutOfBounds: Boolean = false,
    ): List<PitchCoordinate>
}
