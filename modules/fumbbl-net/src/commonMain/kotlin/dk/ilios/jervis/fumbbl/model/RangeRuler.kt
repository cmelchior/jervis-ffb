package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.model.change.PlayerId
import kotlinx.serialization.Serializable

@Serializable
data class RangeRuler(
    val throwerId: PlayerId,
    val targetCoordinate: FieldCoordinate,
    val minimumRoll: Int,
    val throwTeamMate: Boolean
)
