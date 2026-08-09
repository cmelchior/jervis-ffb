package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.model.RerollSourceId
import kotlinx.serialization.Serializable

/** What the coach selected and what the rules ultimately allowed. */
@Serializable
data class ChanceRerollSelection(
    val sourceId: RerollSourceId,
    val resultIds: List<ChanceResultId>,
    val allowed: Boolean? = null,
    val aborted: Boolean = false,
)
