package com.jervisffb.engine.model.inducements.settings

/**
 * A list of all available inducement types across all rulesets.
 * Which inducement is available for a given ruleset is defined in
 * [com.jervisffb.engine.InducementSettings].
 *
 * See page 89 in the BB2020 rulebook.
 * See page 142 in the BB2025 rulebook
 */
enum class InducementType {

    // Common Inducements for BB2025 (and those shared with BB2020)
    // See page 89 in the BB2020 rulebook
    // See page 142 in the BB2025 rulebook
    PRAYERS_TO_NUFFLE,
    PART_TIME_ASSISTANT_COACH,
    TEMP_AGENCY_CHEERLEADER,
    TEAM_MASCOT,
    WEATHER_MAGE,
    BLITZERS_BEST_KEGS,
    BRIBE,
    EXTRA_TEAM_TRAINING,
    MORTUARY_ASSISTANT,
    PLAGUE_DOCTOR,
    RIOTOUS_ROOKIE,
    WANDERING_APOTHECARY,
    HALFLING_MASTER_CHEF,
    BIASED_REFEREE,
    INFAMOUS_COACHING_STAFF,
    STANDARD_MERCENARY_PLAYERS,
    STAR_PLAYERS,
    WIZARD,

    // Common Inducements for BB2020 that are not in BB2025.
    BLOODWEISER_KEG,
    SPECIAL_PLAY,

    // BB2020 DeathZone
    // ...
    WAAAGH_DRUMMER,
    CAVORTING_NURGLINGS,
    DWARFEN_RUNESMITH,
    HALFLING_HOTPOT,
    MASTER_OF_BALLISTICS,
    EXPANDED_MERCENARY_PLAYERS, // Contains a lot of sub options
    GIANT,
    DESPERATE_MEASURES, // Only available for BB7

    // Spike 19
    BRETONNIAN_PASTRIES,
    BRETONNIAN_DAMSEL,

    // Spike 20
    CANOPIC_JAR,

    // Other extensions
    // ...
}
