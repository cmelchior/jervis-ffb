package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import kotlinx.serialization.Serializable

/** A stable, consumable recovery resource referenced by one or more events. */
@Serializable
data class RerollResource(
    val id: RerollSourceId,
    val owner: TeamId,
    val category: RerollCategory,
    val usage: RerollUsage,
)

