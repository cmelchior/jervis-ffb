package com.jervisffb.engine.statistics.probability.event

import com.jervisffb.engine.model.PlayerId
import kotlinx.serialization.Serializable

/**
 * ID used to identify the resource-reset boundaries containing a chance event.
 */
@Serializable
data class ActionPathEventScope(
    val half: Int,
    val drive: Int,
    val turn: Int,
    val player: PlayerId? = null,
)

