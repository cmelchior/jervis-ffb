package com.jervisffb.engine.model

/**
 * Enum used to track the underlying reason for a Turnover.
 */
enum class TurnOver {
    // Any of the normal reasons listed on page 23 in the BB2020 rulebook.
    // Any of the normal reasons listed on page 35 in the BB2025 rulebook.
    STANDARD,
    // The active team scored a touchdown
    ACTIVE_TEAM_TOUCHDOWN,
    // The inactive team scored a touch
    INACTIVE_TEAM_TOUCHDOWN,
    // Active players turn timing out is not strictly a turn-over, but in some
    // cases we want to treat it as one.
    TIME_OUT,
}

