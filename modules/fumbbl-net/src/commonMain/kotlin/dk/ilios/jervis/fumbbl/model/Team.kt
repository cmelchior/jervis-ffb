package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    val teamId: String,
    val teamName: String,
    val coach: String,
    val race: String,
    val reRolls: Int,
    val apothecaries: Int,
    val cheerleaders: Int,
    val assistantCoaches: Int,
    val fanFactor: Int,
    val teamValue: Int,
    val treasury: Int,
    val baseIconPath: String, // TODO Turn into URL?
    val logoUrl: String?,
    val dedicatedFans: Int,
    @SerialName("playerArray")
    val players: Array<Player>,
    val specialRules: Set<SpecialRule>,
    val roster: Roster
//    val fInducementSet: InducementSet? = null
)