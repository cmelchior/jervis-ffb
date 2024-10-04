package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Loner(
    override val value: Int,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val skillId: String = "loner-skill"
    override val name: String = "Loner ($value+)"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data class Factory(override val value: Int): SkillFactory {
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = Loner(value, isTemporary, expiresAt)
    }
}
