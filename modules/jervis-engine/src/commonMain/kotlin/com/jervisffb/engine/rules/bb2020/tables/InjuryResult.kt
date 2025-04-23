package com.jervisffb.engine.rules.bb2020.tables

/**
 * List all results of rolling on the Injury Table, both Stunty and Normal.
 * This also includes all variants like BB7 and Dungeon Bowl. Which results
 * are used are determined the relevant [StandardInjuryTable].
 */
enum class InjuryResult(val title: String) {
    STUNNED("Stunned"),
    KO("KO'd"),
    BADLY_HURT("Badly Hurt"),
    SERIOUSLY_HURT("Seriously Hurt"),
    DEAD("DEAD"),
    // This requires a roll on the Casualty Table to determine the final result
    CASUALTY("Casualty"),
}
