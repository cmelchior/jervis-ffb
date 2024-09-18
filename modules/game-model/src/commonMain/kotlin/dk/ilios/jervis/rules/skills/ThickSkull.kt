package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Thick Skull skill.
 *
 * See page 80 in the rulebook.
 */
@Serializable
class ThickSkull(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill{
    override val skillId: String = "thick-skull-skill"
    override val name: String = "Thick Skull"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.STRENGTH
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = ThickSkull(isTemporary, expiresAt)
    }
}
