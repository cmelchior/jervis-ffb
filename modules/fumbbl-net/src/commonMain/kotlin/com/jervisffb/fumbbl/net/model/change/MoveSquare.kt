package com.jervisffb.fumbbl.net.model.change

import com.jervisffb.fumbbl.net.model.FieldCoordinate
import kotlinx.serialization.Serializable

@Serializable
data class MoveSquare(
    val coordinate: FieldCoordinate,
    val minimumRollDodge: Int,
    val minimumRollGfi: Int,
)
