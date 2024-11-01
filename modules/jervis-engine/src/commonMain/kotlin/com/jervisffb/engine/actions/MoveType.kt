package com.jervisffb.engine.actions

import kotlinx.serialization.Serializable

/**
 * What kind of move does the player want to perform
 */
@Serializable
enum class MoveType {
    JUMP,
    LEAP,
    STANDARD,
    STAND_UP,
}

