package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.Agility
import kotlinx.serialization.Serializable

@Serializable
class CatchSkill: BB2020Skill, D6StandardSkillReroll {
    override val id: String = "catch-skill"
    override val name: String = "Catch"
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val limit: Int = Int.MAX_VALUE
    override val category: SkillCategory = Agility
    override var used: Int = 0

    override val rerollDescription: String = "Catch Reroll"
    override var rerollUsed: Boolean = false // Catch is always available

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*, *>>, wasSuccess: Boolean?): Boolean {
        return type == DiceRollType.CatchRoll
    }

    @Serializable
    companion object : SkillFactory {
        override fun createSkill(): CatchSkill = CatchSkill()
    }
}