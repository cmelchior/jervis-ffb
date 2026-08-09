package com.jervisffb.engine.statistics.probability.observation

import kotlinx.serialization.Serializable

/** The engine concept that supplied a reroll. This does not assign scoring priority. */
@Serializable
enum class ChanceRerollSourceKind {
    SKILL,
    TEAM_REROLL,
    OTHER,
}
