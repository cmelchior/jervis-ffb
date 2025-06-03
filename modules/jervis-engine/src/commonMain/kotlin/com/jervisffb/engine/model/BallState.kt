package com.jervisffb.engine.model

enum class BallState {
    ACCURATE_THROW, // Ball is currently traveling from parser to catcher in the air and the throw was accurate
    BOUNCING, // Ball was just dropped, will bounce before going to either ON_GROUND, CARRIED or OUT_OF_BOUNDS
    CARRIED, // Ball is being carried by whatever player is also in the field
    DEVIATING, // Ball is currently traveling from parser to catcher in the air but is deviating
    IN_AIR, // Ball is high in the air. It is not possible to catch it
    ON_GROUND, // Ball is on the ground and is catchable
    OUT_OF_BOUNDS, // Ball left the field.
    SCATTERED, // Ball is in the air and scattering
    THROW_IN, // Ball is in the process of being thrown in after going OUT_OF_BOUNDS
}
