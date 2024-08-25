package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Block : BB2020Skill {
    override val id: String = Factory.ID
    override val name: String = "Block"
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val limit: Int = Int.MAX_VALUE
    override val category: SkillCategory = General
    override var used: Int = 0

    @Serializable
    data object Factory: SkillFactory {
        val ID = "block-skill"
        override fun createSkill() = Block()
    }
}
