package dk.ilios.jervis.model

enum class BallState {
    CARRIED, // Ball is being carried by whatever player is also in the field
    IN_AIR,  // Ball is high in the air. It is not possible to catch it
    ON_GROUND, // Ball is on the ground and is catchable
    DROPPED // Ball was just dropped, will bounce before b
}

class Ball {
    var state: BallState = BallState.ON_GROUND
    var location: FieldCoordinate = FieldCoordinate.UNKNOWN
}