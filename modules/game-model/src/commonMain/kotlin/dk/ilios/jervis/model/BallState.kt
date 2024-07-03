package dk.ilios.jervis.model

enum class BallState {
     ACCURATE_THROW,
    BOUNCING, // Ball was just dropped, will bounce before b
    CARRIED, // Ball is being carried by whatever player is also in the field
    DEVIATING,
    IN_AIR,  // Ball is high in the air. It is not possible to catch it
    ON_GROUND, // Ball is on the ground and is catchable
    OUT_OF_BOUNDS,
    SCATTERED,
    THROW_IN,
}
