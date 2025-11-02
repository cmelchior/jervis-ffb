package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

class Loner(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val value: Int? = 4,
    override val expiresAt: Duration,
) : BB2025IntSkill {
    override val type: SkillType = SkillType.LONER
    override val skillId: SkillId = type.id(value)
    override val name: String = "${type.description}($value+)"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.PASSIVE)
}
