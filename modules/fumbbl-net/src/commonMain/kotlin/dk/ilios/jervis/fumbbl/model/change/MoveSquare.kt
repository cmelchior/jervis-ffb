package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.FieldCoordinate
import kotlinx.serialization.Serializable

@Serializable
data class MoveSquare(
    val coordinate: FieldCoordinate,
    val minimumRollDodge: Int,
    val minimumRollGfi: Int,
)
