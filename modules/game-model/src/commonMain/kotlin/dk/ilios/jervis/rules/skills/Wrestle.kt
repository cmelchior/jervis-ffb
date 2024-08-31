package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Wrestle : BB2020Skill {
    override val id: String = "wrestle-skill"
    override val name: String = "Wrestle"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = General
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Wrestle()
    }
}
