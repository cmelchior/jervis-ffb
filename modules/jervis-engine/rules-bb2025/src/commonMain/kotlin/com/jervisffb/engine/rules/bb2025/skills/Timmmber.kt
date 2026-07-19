package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the "Timmm-ber! (Passive)" skill.
 *
 * See page 137 in the BB2025 rulebook.
 */
class Timmmber(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.TIMMMBER
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.PASSIVE)
}
