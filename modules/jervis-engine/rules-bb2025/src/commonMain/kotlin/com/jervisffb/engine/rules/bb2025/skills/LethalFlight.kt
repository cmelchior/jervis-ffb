package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Lethal Flight (Active)" skill.
 *
 * See page 130 in the BB2025 rulebook.
 */
class LethalFlight(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.DEVIOUS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.LETHAL_FLIGHT
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    // Must reset after resolving each injury, as it can be used on each player it lands on
    override val resetAt: Duration = Duration.SPECIAL
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
