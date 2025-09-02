package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

/**
 * Enumerate the different "main" action types available in Blood Bowl.
 * The special category should only be used for skills that are completely
 * their own, and doesn't replace a skill in one of the other categories.
 */
@Serializable
enum class PlayerStandardActionType: ActionType {
    BLITZ,
    BLOCK,
    FOUL,
    HAND_OFF,
    MOVE,
    PASS,
    SECURE_THE_BALL,
    SPECIAL,
    THROW_TEAM_MATE,
}
