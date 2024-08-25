package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Dodge : BB2020Skill {
    override val id: String = "dodge-skill"
    override val name: String = "Dodge"
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.END_OF_TURN
    override val limit: Int = 1
    override val category: SkillCategory = General
    override var used: Int = 0

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Dodge()
    }
}
