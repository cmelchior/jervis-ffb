package com.jervisffb.engine.statistics.probability.event

import kotlinx.serialization.Serializable

/** A recovery source the coach demonstrably attempted to use. */
@Serializable
data class ActualRerollUse(
    val resource: RerollResource,
    val description: String,
)
