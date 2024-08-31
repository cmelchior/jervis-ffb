package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.Traits
import kotlinx.serialization.Serializable

/**
 * Representation of the Prehensile Tail skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class PrehensileTail : BB2020Skill{
    override val id: String = "prehensile-tail-skill"
    override val name: String = "Prehensile Tail"
    override val compulsory: Boolean = false
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val category: SkillCategory = Traits
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = PrehensileTail()
    }
}
