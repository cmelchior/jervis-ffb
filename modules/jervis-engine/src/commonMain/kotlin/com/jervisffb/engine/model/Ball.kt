package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate

class Ball {
    var state: BallState = BallState.ON_GROUND
    var location: FieldCoordinate = FieldCoordinate.UNKNOWN

    // Only != null if CARRIED
    var carriedBy: Player? = null

    // Only set if state = OUT_OF_BOUNDS
    var outOfBoundsAt: FieldCoordinate? = null
}
