package com.jervisffb.ui.markings

import com.jervisffb.engine.model.PlayerId
import kotlinx.serialization.Serializable

@Serializable
data class PlayerMarkingsSettings(
    val enabled: Boolean = true,
    val markings: List<PlayerMarking> = defaultPlayerMarkings,
    val playerOverrides: Map<PlayerId, String> = emptyMap(),
)
