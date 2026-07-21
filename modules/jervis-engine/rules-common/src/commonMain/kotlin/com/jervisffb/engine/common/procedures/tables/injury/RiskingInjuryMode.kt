package com.jervisffb.engine.common.procedures.tables.injury

enum class RiskingInjuryMode {
    PLACED_PRONE, // Not really an Injury, but easier to track through this
    FALLING_OVER,
    KNOCKED_DOWN,
    PUSHED_INTO_CROWD, // Or fallen through a Trapdoor
    FOUL,
    HIT_BY_ROCK,
    // Player is injured after being thrown (normally the same as Falling Over),
    // but we need to know the difference in order to trigger turnovers
    // correctly.
    BAD_LANDING,
    STAB, // Armour/Injury is rolled as part of a Stab
    PROJECTILE_VOMIT, // Armour/Injury is rolled as part of a Projectile Vomit attack
    CHAINSAW, // Armour/Injury is rolled as part of a Chainsaw attack
}
