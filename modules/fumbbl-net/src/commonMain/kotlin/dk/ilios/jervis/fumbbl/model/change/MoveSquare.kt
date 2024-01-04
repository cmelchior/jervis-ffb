package dk.ilios.jervis.fumbbl.model.change

import kotlinx.serialization.Serializable

@Serializable
data class MoveSquare(
    val coordinate: List<Int>,
    val minimumRollDodge: Int,
    val minimumRollGfi: Int
)