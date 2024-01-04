package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Roster(
    val rosterId: String,
    val rosterName: String,
    val reRollCost: Int,
    val maxReRolls: Int,
    val baseIconPath: String,
    val logoUrl: String?,
    val raisedPositionId: String,
    val apothecary: Boolean,
    val necromancer: Boolean,
    val undead: Boolean,
    val riotousPositionId: String?,
    val nameGenerator: String?,
    val maxBigGuys: Int,
    @SerialName("positionArray")
    val positions: Array<RosterPlayer>
)
