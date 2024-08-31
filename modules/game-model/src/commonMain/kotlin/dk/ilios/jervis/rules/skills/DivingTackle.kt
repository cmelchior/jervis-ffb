package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.Traits
import kotlinx.serialization.Serializable

/**
 * Representation of the Diving Tackle skill.
 *
 * See page 75 in the rulebook.
 */
@Serializable
class DivingTackle : BB2020Skill{
    override val id: String = "diving-tackle-skill"
    override val name: String = "Diving Tackle"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = Traits
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = DivingTackle()
    }
}
