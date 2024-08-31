package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.Agility
import kotlinx.serialization.Serializable

@Serializable
class SureFeet : BB2020Skill, D6StandardSkillReroll {
    override val id: String = "sure-feet-skill"
    override val name: String = "Sure Feet"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = Agility
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    override val rerollResetAt: ResetPolicy = ResetPolicy.END_OF_TURN
    override val rerollDescription: String = "Sure Feet Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.PICKUP
    }

    @Serializable
    data object Factory: SkillFactory {
        override fun createSkill() = SureFeet()
    }
}
