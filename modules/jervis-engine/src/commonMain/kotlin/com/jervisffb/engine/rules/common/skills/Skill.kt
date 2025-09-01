package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.common.skills.SkillCategory

/**
 * This interface represents player Skills. Since these skills are stateful
 * they are required to have a [skillId] that is unique across the entire game.
 */
interface Skill {
    // The player this skill is assigne to
    val player: Player
    // Unique identifier for the skill
    // The same skill across multiple players have the same id,
    // so use `player.id + skillId` to uniquely identify a skill
    val skillId: SkillId
    // Skill type, effectively the same as checking the KClass, but enums
    // make it easier to enumerate all options.
    val type: SkillType
    // Represents any value in brackes, like Might Blow(1+) or Loner(4+). It is up to the context to correctly
    // interpret this value
    val value: Int?
    // Which category does this skill belong to?
    val category: SkillCategory
    // Human readable name of this skill
    val name: String
    // Whether this skill is compulsory to use
    val compulsory: Boolean
    // Whether this skill count as being "used". The meaning of this is interpreted in the context it is used.
    // If the skill is always available, this should always be false.
    // Note, this specifically does not apply to a "reroll" part of a skill.
    var used: Boolean
    // When the `used` state reset back to `false`?
    val resetAt: Duration
    // Whether this skill works when the player has lost its tackle zones
    val workWithoutTackleZones: Boolean
    // Whether this skill works when the player is prone or stunned
    val workWhenProne: Boolean
    // If the skill is temporary, this defines when the skill expires and is removed
    val expiresAt: Duration
    // Whether this skill is temporary (removed at latest and end of game) or not
    val isTemporary: Boolean
        get() = (expiresAt != Duration.PERMANENT)
}
