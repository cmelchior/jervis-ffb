package com.jervisffb.fumbbl.net.model.change

import com.jervisffb.fumbbl.net.model.Direction
import com.jervisffb.fumbbl.net.model.FieldCoordinate
import kotlinx.serialization.Serializable

@Serializable
data class PushBackSquare(
    val coordinate: FieldCoordinate,
    val direction: Direction,
    val selected: Boolean,
    val locked: Boolean,
    val homeChoice: Boolean,
)
