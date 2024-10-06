package com.jervisffb.engine.fsm

import com.jervisffb.engine.actions.GameAction

/**
 * Enum describing which team is responsible for creating the [GameAction]
 * required by a [ActionNode].
 */
enum class ActionOwner {
    HOME_TEAM,
    AWAY_TEAM,
}

