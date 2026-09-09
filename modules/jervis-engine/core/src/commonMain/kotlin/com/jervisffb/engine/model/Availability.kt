package com.jervisffb.engine.model

/**
 * Describes the state of a players "availability" during their teams turn.
 * Players should be marked [Availability.AVAILABLE] at the start of a team turn
 * and then moved to other states as appropriate. E.g. they might move directly
 * to [Availability.UNAVAILABLE] if they are stunned.
 */
enum class Availability {
    // Player is available to be activated this turn.
    AVAILABLE,
    // Player is currently active. Note, while this mirrors the "Activated"
    // state in the rulebook, we still allow players to go out of it again as
    // long as they haven't moved or rolled dice.
    IS_ACTIVE,
    // Player has already activated this turn.
    HAS_ACTIVATED,
    // Player is unavailable to activate this turn, e.g. because they are
    // stunned.
    UNAVAILABLE,
}
