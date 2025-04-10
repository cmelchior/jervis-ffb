package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId

/**
 * TODO Missing Reroll support
 */
class Pass(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.PASSING,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill {
    override val type: SkillType = SkillType.PASS
    override val value: Int? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
}
