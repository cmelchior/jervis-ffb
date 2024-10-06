package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.FieldCoordinate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class Ball : Observable<Ball>() {
    var state: BallState = BallState.ON_GROUND
    var location: FieldCoordinate = FieldCoordinate.UNKNOWN

    // Only != null if CARRIED
    var carriedBy: Player? = null

    // Only set if state = OUT_OF_BOUNDS
    var outOfBoundsAt: FieldCoordinate? = null

    @Transient
    val observeBall = observeState
}
