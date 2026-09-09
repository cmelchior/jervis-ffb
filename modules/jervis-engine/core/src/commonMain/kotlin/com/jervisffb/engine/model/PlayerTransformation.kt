package com.jervisffb.engine.model

import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.serialization.PlayerUiData

/**
 * Describes a [Player] that has temporarily been turned into something else,
 * e.g. by the "Zap!" spell turning them into a Frog.
 *
 * A transformed player keeps their identity. The same [Player] instance stays
 * in [Team.noToPlayer], on the pitch, in [Ball.carriedBy] and in any
 * [com.jervisffb.engine.model.context.ProcedureContext], so no reference to
 * them can go out of sync. Only the parts that come from their [Position] are
 * swapped out, i.e. their base stats, positional skills, special rules,
 * keywords and how they are rendered, together with any permanent upgrades,
 * which are suppressed for as long as the transformation lasts.
 *
 * Everything else is deliberately left alone. Temporary stat modifiers,
 * temporary skills and status effects all follow the player into the
 * transformation and back out of it again, including anything picked up while
 * transformed. See "Page 149 - Sports-Wizard" in
 * `website/bb2025/bb2025-base-rules.md` for the reasoning behind that.
 *
 * This class holds everything needed to turn the player back to normal, see
 * [Player.restoreFrom]. Instances are created by
 * [com.jervisffb.engine.commands.TransformPlayer] and removed again by
 * [com.jervisffb.engine.commands.EndPlayerTransformation].
 */
class PlayerTransformation(
    // When the player turns back to normal again.
    val expiresAt: Duration,
    // The player as they were just before being transformed.
    val position: Position,
    val icon: PlayerUiData?,
    val baseMove: Int,
    val baseStrength: Int,
    val baseAgility: Int,
    val basePassing: Int?,
    val baseArmorValue: Int,
    val positionSkills: List<Skill<*>>,
    val positionSpecialRules: List<PlayerSpecialRule>,
    val keywords: List<PlayerKeyword>,
    // Level-ups are lost while transformed, so they are parked here until the
    // player turns back to normal.
    val suppressedSkills: List<Skill<*>>,
    val suppressedStatModifiers: List<StatModifier>,
) {
    companion object {
        /**
         * Capture everything needed to turn [player] back to what they are
         * right now, once a transformation lasting until [expiresAt] ends.
         *
         * This captures the live [Skill] instances, so restoring the player
         * also restores whether those skills had already been used.
         */
        fun capture(player: Player, expiresAt: Duration): PlayerTransformation {
            return PlayerTransformation(
                expiresAt = expiresAt,
                position = player.position,
                icon = player.icon,
                baseMove = player.baseMove,
                baseStrength = player.baseStrength,
                baseAgility = player.baseAgility,
                basePassing = player.basePassing,
                baseArmorValue = player.baseArmorValue,
                positionSkills = player.positionSkills.toList(),
                positionSpecialRules = player.positionSpecialRules.toList(),
                keywords = player.keywords.toList(),
                suppressedSkills = player.extraSkills.filter { !it.isTemporary },
                suppressedStatModifiers = player.statModifiers.filter { it.expiresAt == Duration.PERMANENT },
            )
        }
    }
}
