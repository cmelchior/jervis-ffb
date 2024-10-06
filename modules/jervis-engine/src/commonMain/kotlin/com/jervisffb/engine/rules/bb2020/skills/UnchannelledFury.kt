package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Unchannelled Fury* skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class UnchannelledFury(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val skillId: String = "unchannelled-fury-skill"
    override val name: String = "Unchannelled Fury"
    override val compulsory: Boolean = true
    override val resetAt: Duration =
        Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            UnchannelledFury(isTemporary, expiresAt)
    }
}
