package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Dirty Player" skill.
 *
 * See page 127 in the BB2025 rulebook.
 */
class DirtyPlayer(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.DEVIOUS,
    override val value: Int = 1,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025IntSkill {
    override val type: SkillType = SkillType.DIRTY_PLAYER
    override val skillId: SkillId = type.idAdjustment(value)
    override val name: String = buildString {
        append(type.description)
        if (value > 1) append("(+$value)")
    }
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.END_OF_ACTION
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
