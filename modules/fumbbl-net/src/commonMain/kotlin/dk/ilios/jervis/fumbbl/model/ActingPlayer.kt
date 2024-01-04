package dk.ilios.jervis.fumbbl.model

import kotlinx.serialization.Serializable

@Serializable
data class ActingPlayer(
    val playerId: String?,
    val currentMove: Int,
    val goingForIt: Boolean,
    val hasBlocked: Boolean,
    val hasFed: Boolean,
    val hasFouled: Boolean,
    val hasMoved: Boolean,
    val hasPassed: Boolean,
    val playerAction: PlayerAction?,
    val standingUp: Boolean,
    val sufferingAnimosity: Boolean,
    val sufferingBloodlust: Boolean,
    val fumblerooskiePending: Boolean,
    val usedSkills: List<String>,
    val skillsGrantedBy: Map<String, String?>,
    val playerStateOld: PlayerAction?
)