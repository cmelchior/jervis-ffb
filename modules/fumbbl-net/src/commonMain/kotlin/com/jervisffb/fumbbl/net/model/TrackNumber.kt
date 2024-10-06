package com.jervisffb.fumbbl.net.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackNumber(
    val number: Int,
    val coordinate: List<Int>,
)
