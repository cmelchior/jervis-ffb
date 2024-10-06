package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Horns skill.
 *
 * See page 78 in the rulebook.
 */
@Serializable
class Horns(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val skillId: String = "horns-skill"
    override val name: String = "Horns"
    override val compulsory: Boolean = false
    override val resetAt: Duration =
        Duration.PERMANENT // Does this ever counts as "used". Probably not since it works every time.
    override val category: SkillCategory = BB2020SkillCategory.MUTATIONS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            Horns(isTemporary, expiresAt)
    }
}
