package com.jervisffb.fumbbl.net.model

import com.jervisffb.fumbbl.net.model.change.PlayerId
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
    val skillsGrantedBy: MutableMap<String, String?>,
    var playerStateOld: PlayerState?,
) {
    fun markSkillUsed(skill: String) {
        usedSkills.add(skill)
    }

    fun markSkillUnused(skill: String) {
        usedSkills.add(skill)
    }

    fun updatePlayerId(id: PlayerId?) {
        if (id == playerId) return
        playerId = id
        playerStateOld = null
        usedSkills.clear()
        currentMove = 0
        goingForIt = false
        dodging = false
        hasBlocked = false
        hasFouled = false
        hasPassed = false
        hasMoved = false
        hasFed = false
        jumping = false
        playerAction = null
        standingUp = false
        sufferingBloodlust = false
        sufferingAnimosity = false
        hasJumped = false
        // wasProne = false
        fumblerooskiePending = false
//        val player: Player<*> = getGame().getPlayerById(getPlayerId())
//        setStrength(if (player != null) player.getStrengthWithModifiers() else 0)
        skillsGrantedBy.clear()
    }

}
