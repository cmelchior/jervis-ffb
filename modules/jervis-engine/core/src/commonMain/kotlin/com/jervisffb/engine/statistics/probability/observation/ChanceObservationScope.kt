package com.jervisffb.engine.statistics.probability.observation

import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.TeamId
import kotlinx.serialization.Serializable

/**
 * Identifies the game boundaries containing a random event.
 * TODO Do we strictly need this?
 */
@Serializable
data class ChanceObservationScope(
    val half: Int,
    val drive: Int,
    val team: TeamId,
    val turn: Int,
    val player: PlayerId? = null,
)
