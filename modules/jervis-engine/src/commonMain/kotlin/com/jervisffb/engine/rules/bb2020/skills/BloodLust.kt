package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId

/**
 * Representation of the Blood Lust(X+)* skill.
 *
 * See Spike Magazin XXX
 */
class BloodLust(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val value: Int? = 4,
    override val expiresAt: Duration,
) : BB2020Skill {
    override val type: SkillType = SkillType.BLOOD_LUST
    override val skillId: SkillId = type.id(value)
    override val name: String = "${type.description}($value+)"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
}
