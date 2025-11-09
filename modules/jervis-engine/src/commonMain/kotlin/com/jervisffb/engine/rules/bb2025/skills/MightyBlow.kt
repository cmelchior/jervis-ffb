package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

class MightyBlow(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.STRENGTH,
    override val expiresAt: Duration,
) : BB2025IntSkill {
    override val value: Int = 1
    override val type: SkillType = SkillType.MIGHTY_BLOW
    override val skillId: SkillId = type.id(value)
    override val name: String = buildString {
        append(type.description)
        if (value > 1) append("(+$value)")
    }
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(
        SkillKeyword.ACTIVE,
        SkillKeyword.ELITE
    )
}
