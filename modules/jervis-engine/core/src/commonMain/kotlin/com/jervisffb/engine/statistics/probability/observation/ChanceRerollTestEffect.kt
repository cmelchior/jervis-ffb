package com.jervisffb.engine.statistics.probability.observation

import kotlinx.serialization.Serializable

/** The factual effect of a test performed while using a reroll source. */
@Serializable
enum class ChanceRerollTestEffect {
    ALLOWS_REROLL,
    RESTORES_SOURCE,
}
