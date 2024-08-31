package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Block : BB2020Skill {
    override val id: String = "block-skill"
    override val name: String = "Block"
    override val compulsory: Boolean = false
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val category: SkillCategory = General
    override var used: Boolean = false // This skill is always available
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Block()
    }
}
