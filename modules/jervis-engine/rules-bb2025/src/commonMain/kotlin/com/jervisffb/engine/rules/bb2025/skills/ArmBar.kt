package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Arm Bar (Active)" skill.
 *
 * See page 123 in the BB2025 rulebook.
 */
class ArmBar(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.STRENGTH,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.ARM_BAR
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
