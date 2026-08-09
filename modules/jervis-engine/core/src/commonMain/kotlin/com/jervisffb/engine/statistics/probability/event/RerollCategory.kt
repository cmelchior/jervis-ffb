package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/**
 * [RerollResource] are grouped into categories, allowing policies to
 * order them based on the category. This makes it possible to create heuristics
 * for choosing the optimal reroll, which is far cheaper than using Dynamic
 * Programming to find them.
 *
 * TODO Expand these categories to cover the categories we care about.
 */
@Serializable
enum class RerollCategory {
    STANDARD_SKILL, // "free" skill rerolls (that might only apply to a single dice roll type)
    PRO, // Pro is special because it provides a flexible reroll that is guarded by its own roll
    TEAM_REROLL, // A team reroll that applies to the curren team turn
}


