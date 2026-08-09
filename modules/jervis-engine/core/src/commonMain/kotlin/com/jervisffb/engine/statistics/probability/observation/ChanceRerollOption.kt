package com.jervisffb.engine.statistics.probability.observation

import kotlinx.serialization.Serializable

/** One set of results a source can reroll and the branches where it applies. */
@Serializable
data class ChanceRerollOption(
    val source: ChanceRerollSource,
    val resultIds: List<ChanceResultId>,
    val appliesOnSuccess: Boolean,
    val appliesOnFailure: Boolean,
    val currentlyAvailable: Boolean,
)
