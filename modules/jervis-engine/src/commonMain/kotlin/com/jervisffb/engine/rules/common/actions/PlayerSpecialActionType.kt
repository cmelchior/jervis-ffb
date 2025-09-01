package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

/**
 * Enumerate the different special action types possible in Blood Bowl.
 * All special actions are tied to skills.
 */
@Serializable
enum class PlayerSpecialActionType: ActionType {
    BALL_AND_CHAIN,
    BOMBARDIER,
    BREATHE_FIRE,
    CHAINSAW,
    HYPNOTIC_GAZE,
    KICK_TEAM_MATE,
    MULTIPLE_BLOCK,
    PROJECTILE_VOMIT,
    STAB,
}
