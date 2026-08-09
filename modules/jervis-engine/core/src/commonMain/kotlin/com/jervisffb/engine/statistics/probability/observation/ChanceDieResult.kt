package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.DieId
import kotlinx.serialization.Serializable

/** One physical die result, optionally linked to the engine's logical die. */
@Serializable
data class ChanceDieResult(
    val id: ChanceResultId,
    val result: DieResult,
    val dieId: DieId? = null,
)
