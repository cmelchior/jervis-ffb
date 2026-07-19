package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Shadowing" skill.
 *
 * See page 135 in the BB2025 rulebook.
 */
class Shadowing(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.DEVIOUS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.SHADOWING
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.SPECIAL
    // For Shadowing, it can be used up to the players MA stat, so we track how many times it has been used
    // so we can disable it (used = false) at the right time.
    var usedThisTurn: Int = 0
    override var used: Boolean = false
        get() {
            return usedThisTurn >= player.move
        }
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
