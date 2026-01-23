package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Quick Foul" skill.
 *
 * See page 134 in the BB2025 rulebook.
 */
class QuickFoul(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.DEVIOUS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.QUICK_FOUL
    override val value: Unit? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
