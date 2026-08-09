package com.jervisffb.engine.statistics.probability.observation

import kotlinx.serialization.Serializable

/** Identifies one result within one physical dice roll. */
@Serializable
data class ChanceResultId(
    val rollIndex: Int,
    val resultIndex: Int,
)
