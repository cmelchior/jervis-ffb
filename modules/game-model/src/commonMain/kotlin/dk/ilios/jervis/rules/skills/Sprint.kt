package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Represents the "Sprint" skill.
 *
 * See page 75 in the rulebook.
 */
@Serializable
class Sprint(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill {
    override val id: String = "sprint-skill"
    override val name: String = "Sprint"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = Sprint(isTemporary, expiresAt)
    }
}
