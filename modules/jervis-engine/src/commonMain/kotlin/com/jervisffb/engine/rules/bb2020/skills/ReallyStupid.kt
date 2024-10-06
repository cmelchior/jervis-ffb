package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Bone Head* skill.
 *
 * See page 86 in the rulebook.
 */
@Serializable
class ReallyStupid(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val skillId: String = "really-stupid-skill"
    override val name: String = "Really Stupid"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = ReallyStupid(isTemporary, expiresAt)
    }
}
