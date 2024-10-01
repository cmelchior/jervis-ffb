package dk.ilios.jervis.model

/**
 * Enum used to track the underlying reason for a Turnover.
 */
enum class TurnOver {
    // Any of the normal reasons listed on page 23 in the rulebook
    STANDARD,
    // The active team scored a touchdown
    ACTIVE_TEAM_TOUCHDOWN,
    // The inactive team scored a touch
    INACTIVE_TEAM_TOUCHDOWN,
}

