package dk.ilios.jervis.fumbbl.model

import dk.ilios.jervis.fumbbl.model.change.PlayerId
import kotlinx.serialization.Serializable

@Serializable
data class ActingPlayer(
    var playerId: PlayerId?,
    var strenght: Int = 0,
    var currentMove: Int,
    var goingForIt: Boolean,
    var dodging: Boolean = false,
    var jumping: Boolean = false,
    var hasBlocked: Boolean,
    var hasFed: Boolean,
    var hasFouled: Boolean,
    var hasJumped: Boolean = false,
    var hasMoved: Boolean,
    var hasPassed: Boolean,
    var playerAction: PlayerAction?,
    var standingUp: Boolean,
    var sufferingAnimosity: Boolean,
    var sufferingBloodlust: Boolean,
    var fumblerooskiePending: Boolean,
    val usedSkills: MutableList<String>,
    val skillsGrantedBy: Map<String, String?>,
    var playerStateOld: PlayerState?
) {
    fun markSkillUsed(skill: String) {
        usedSkills.add(skill)
    }

    fun markSkillUnused(skill: String) {
        usedSkills.add(skill)
    }
}