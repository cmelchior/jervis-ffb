package dk.ilios.jervis.model

enum class BallState {
    CARRIED, // Ball is being carried by whatever player is also in the field
    IN_AIR,  // Ball is high in the air. It is not possible to catch it
    ON_GROUND, // Ball is on the ground and is catchable
    DEVIATING,
    BOUNCING, // Ball was just dropped, will bounce before b
    OUT_OF_BOUNDS,
}

class Ball {
    var state: BallState = BallState.ON_GROUND
    var location: FieldCoordinate = FieldCoordinate.UNKNOWN

    // Only != null if CARRIED
    var carriedBy: Player? = null

    // Only set if state = OUT_OF_BOUNDS
    var outOfBoundsAt: FieldCoordinate? = null
}