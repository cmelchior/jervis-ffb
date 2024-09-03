package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Titchy* skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class Titchy(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill{
    override val id: String = "titchy-skill"
    override val name: String = "Titchy"
    override val compulsory: Boolean = true
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = Titchy(isTemporary, expiresAt)
    }
}
