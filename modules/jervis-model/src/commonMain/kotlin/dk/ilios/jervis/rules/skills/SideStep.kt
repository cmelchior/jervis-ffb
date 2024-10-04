package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Sidestep skill.
 *
 * See page XX in the rulebook.
 */
@Serializable
class SideStep(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill{
    override val skillId: String = "sidestep-skill"
    override val name: String = "Sidestep"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = SideStep(isTemporary, expiresAt)
    }
}
