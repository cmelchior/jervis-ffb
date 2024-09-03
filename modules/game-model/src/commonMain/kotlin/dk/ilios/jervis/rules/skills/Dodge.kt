package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Dodge(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill, D6StandardSkillReroll {
    override val id: String = "dodge-skill"
    override val name: String = "Dodge"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    override val rerollDescription: String = "Dodge Reroll"
    override val rerollResetAt: ResetPolicy = ResetPolicy.END_OF_TURN
    override var rerollUsed: Boolean = false

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*, *>>, wasSuccess: Boolean?): Boolean {
        return type == DiceRollType.DODGE
    }

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = Dodge(isTemporary, expiresAt)
    }
}
