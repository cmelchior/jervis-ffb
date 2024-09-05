package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Stab(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val id: String = "stab-skill"
    override val name: String = "Stab"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false // This skill is always available
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = Stab(isTemporary, expiresAt)
    }
}
