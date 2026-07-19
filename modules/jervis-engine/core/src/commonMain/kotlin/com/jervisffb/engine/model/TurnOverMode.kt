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
    // While a time-out was a concept defined in earlier versions of the rules,
    // the concept doesn't exist in BB20205. However, in some cases, we still
    // want to treat it as one.
    TIME_OUT,
}

