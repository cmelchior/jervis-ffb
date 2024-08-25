package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.General
import kotlinx.serialization.Serializable

@Serializable
class Wrestle : BB2020Skill, D6StandardSkillReroll {
    override val id: String = "wrestle-skill"
    override val name: String = "Wrestle"
    override val resetAt: Skill.ResetPolicy = Skill.ResetPolicy.NEVER
    override val limit: Int = Int.MAX_VALUE
    override val category: SkillCategory = General
    override var used: Int = 0

    override val rerollDescription: String = "Block Reroll"
    override var rerollUsed: Boolean = false // Wrestle is always available

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.BlockRoll
    }

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = Wrestle()
    }
}
