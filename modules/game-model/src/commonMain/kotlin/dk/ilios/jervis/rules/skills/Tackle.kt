package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Tackle : BB2020Skill {
    override val id: String = "tackle-skill"
    override val name: String = "Tackle"
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val limit: Int = Int.MAX_VALUE
    override val category: SkillCategory = General
    override var used: Int = 0

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Tackle()
    }
}
