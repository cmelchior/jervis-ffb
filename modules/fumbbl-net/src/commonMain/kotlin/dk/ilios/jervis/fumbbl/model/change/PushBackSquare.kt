package dk.ilios.jervis.fumbbl.model.change

import dk.ilios.jervis.fumbbl.model.Direction
import dk.ilios.jervis.fumbbl.model.FieldCoordinate
import kotlinx.serialization.Serializable

@Serializable
data class PushBackSquare(
    val coordinate: FieldCoordinate,
    val direction: Direction,
    val selected: Boolean,
    val locked: Boolean,
    val homeChoice: Boolean
)
