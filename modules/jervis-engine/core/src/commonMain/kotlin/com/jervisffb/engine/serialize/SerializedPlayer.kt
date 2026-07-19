package com.jervisffb.engine.serialize

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerLevel
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Variant of [com.jervisffb.engine.model.Player] that is optimized for being
 * saved to disk or sent across a network protocol. We use a separate class
 * to make the conversion more explicit. It also allows more flexibility with
 * regard to how we restore teams from a saved state.
 *
 * Note that roster information is still the same between the two models. This
 * is mostly because [com.jervisffb.engine.rules.common.roster.Position]
 * is simple enough that it is safe to share it. The only problem might be the
 * skill factories, but we will cross that bridge when needed.
 *
 * This class represents the player state before starting a game.
 *
 * A players stats is defined by "positionStats + statModifiers"
 */
@Serializable
class SerializedPlayer(
    val id: PlayerId,
    var name: String,
    var number: PlayerNo,
    val position: PositionId,
    val type: PlayerType,
    val statModifiers: List<StatModifier>,
    // Extra skills on top of positional skills.
    // String = SkillId.serialize()
    val extraSkills: List<String>,
    val nigglingInjuries: Int,
    val missNextGame: Boolean,
    val starPlayerPoints: Int,
    val level: PlayerLevel,
    val cost: Int,
    val icon: PlayerUiData?,
) {
    companion object {
        fun serialize(player: Player): SerializedPlayer {
            return SerializedPlayer(
                id = player.id,
                name = player.name,
                number = player.number,
                position = player.position.id,
                type = player.type,
                statModifiers = player.moveModifiers.filter { it.expiresAt == Duration.PERMANENT }
                    + player.strengthModifiers.filter { it.expiresAt == Duration.PERMANENT }
                    + player.agilityModifiers.filter { it.expiresAt == Duration.PERMANENT }
                    + player.passingModifiers.filter { it.expiresAt == Duration.PERMANENT }
                    + player.armourModifiers.filter { it.expiresAt == Duration.PERMANENT },
                extraSkills = player.extraSkills.map { it.skillId.serialize() },
                nigglingInjuries = player.nigglingInjuries,
                missNextGame = player.missNextGame,
                starPlayerPoints = player.starPlayerPoints,
                level = player.level,
                cost = player.cost,
                icon = player.icon,
            )
        }
    }
}
