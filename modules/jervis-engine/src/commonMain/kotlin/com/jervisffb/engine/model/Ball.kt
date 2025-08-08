package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate

class Ball {
    var state: BallState = BallState.ON_GROUND

    // TODO Giant Support
    // Returns the balls location.
    // If carried, UNKNOWN is returned, use `resolvedLocation` instead.
    var location: FieldCoordinate = FieldCoordinate.UNKNOWN

    // Only != null if CARRIED
    var carriedBy: Player? = null

    // Only set if state = OUT_OF_BOUNDS
    var outOfBoundsAt: FieldCoordinate? = null

    /**
     * Returns the ball location, taking into account that it might be carried
     * by a player, in which case the players coordinate is returned.
     */
    fun resolvedLocation(): FieldCoordinate {
        return carriedBy?.let {
            it.coordinates
        } ?: location
    }
}
