package com.jervisffb.engine.actions

import kotlinx.serialization.Serializable

/**
 * This enum represents all possible ways a player can move voluntarily
 * across all rulesets.
 */
@Serializable
enum class MoveType {
    JUMP,
    LEAP,
    STANDARD,
    STAND_UP,
    POGO
}

