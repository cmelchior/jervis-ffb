package dk.ilios.jervis.fsm

import dk.ilios.jervis.actions.GameAction

/**
 * Enum describing which team is responsible for creating the [GameAction]
 * required by a [ActionNode].
 */
enum class ActionOwner {
    HOME_TEAM,
    AWAY_TEAM,
}

