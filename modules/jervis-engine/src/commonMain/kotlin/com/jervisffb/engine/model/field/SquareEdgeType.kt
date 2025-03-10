package com.jervisffb.engine.model.field

import kotlinx.serialization.Serializable

/**
 * This enum describes the behavior of a square's edge.
 * Note,
 */
@Serializable
enum class SquareEdgeType {
    /**
     * The edge is open and has no special properties.
     * This covers the most common use case of moving around on a normal
     * Blood Bowl pitch.
     */
    OPEN,
    /**
     * The edge is open, but crossing it will cause the player or ball
     * to be considered out of bounds.
     * Note, a player moving outside the boundary of a [Field] will
     * always be considered Out-of-Bounds.
     */
    OUT_OF_BOUNDS,
    /**
     * The edge is a wall, preventing movement through it and will affect
     * throws and players being pushed into it.
     */
    WALL,
    /**
     * The edge is a wall, but with a open door, which will mean it is
     * considered [OPEN] for most cases.
     */
    DOOR_OPEN,
}
