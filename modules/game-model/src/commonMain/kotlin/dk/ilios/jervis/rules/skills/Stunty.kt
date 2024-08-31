package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.Traits
import kotlinx.serialization.Serializable

/**
 * Representation of the Stunty* skill.
 *
 * See page 86 in the rulebook.
 */
@Serializable
class Stunty : BB2020Skill{
    override val id: String = "stunty-skill"
    override val name: String = "Stunty"
    override val compulsory: Boolean = true
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = Traits
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Stunty()
    }
}
