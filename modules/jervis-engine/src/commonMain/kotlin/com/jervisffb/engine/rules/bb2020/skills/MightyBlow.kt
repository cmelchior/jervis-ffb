package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

class MightyBlow(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.STRENGTH,
    override val value: Int? = 1,
    override val expiresAt: Duration,
) : BB2020Skill {
    override val type: SkillType = SkillType.MIGHTY_BLOW
    override val skillId: SkillId = type.id(value)
    override val name: String = "${type.description}(+$value)"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
}
