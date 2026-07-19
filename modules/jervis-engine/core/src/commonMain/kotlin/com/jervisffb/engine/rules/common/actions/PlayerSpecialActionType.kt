package com.jervisffb.engine.rules.common.actions

import kotlinx.serialization.Serializable

/**
 * Enumerate the different special action types possible in Blood Bowl across
 * all rulesets. All special actions are tied to skills.
 */
@Serializable
enum class PlayerSpecialActionType: ActionType {
    BALL_AND_CHAIN,
    BOMBARDIER,
    BREATHE_FIRE,
    CHAINSAW,
    CHOMP,
    HYPNOTIC_GAZE,
    KICK_TEAM_MATE,
    MULTIPLE_BLOCK,
    PROJECTILE_VOMIT,
    PUNT,
    STAB,
}
