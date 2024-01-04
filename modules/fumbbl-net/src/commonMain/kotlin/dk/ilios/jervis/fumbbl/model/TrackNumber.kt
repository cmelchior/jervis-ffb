package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackNumber(
    val number: Int,
    val coordinate: List<Int>
)
