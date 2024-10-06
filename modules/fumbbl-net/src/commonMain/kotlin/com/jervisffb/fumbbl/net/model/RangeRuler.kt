package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.model.change.PlayerId
import kotlinx.serialization.Serializable

@Serializable
data class RangeRuler(
    val throwerId: PlayerId,
    val targetCoordinate: FieldCoordinate,
    val minimumRoll: Int,
    val throwTeamMate: Boolean,
)
