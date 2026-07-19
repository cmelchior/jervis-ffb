package com.jervisffb.engine.serialize

import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import kotlinx.serialization.Serializable

/**
 * Variant of [StarPlayerPosition] that is optimized for being
 * saved to disk or sent across a network protocol. We use a separate class
 * to make the conversion more explicit. It also allows more flexibility with
 * regard to how we restore teams from a saved state.
 */
@Serializable
class SerializedStarPlayer(
    val id: PositionId,
    val title: String,
    val shortHand: String,
    val cost: Int,
    val move: Int,
    val strength: Int,
    val agility: Int,
    val passing: Int?,
    val armorValue: Int,
    val skills: List<SkillId>,
    val playsFor: List<RegionalSpecialRule>,
    val icon: SpriteSource?,
    val portrait: SpriteSource?,
) {
    companion object {
        fun serialize(player: StarPlayerPosition): SerializedStarPlayer {
            TODO()
        }
        fun deserialize(playerData: SerializedStarPlayer): StarPlayerPosition {
            TODO()
        }
    }
}
