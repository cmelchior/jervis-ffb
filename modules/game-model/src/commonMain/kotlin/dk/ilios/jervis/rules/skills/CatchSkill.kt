package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class CatchSkill(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill, D6StandardSkillReroll {
    override val id: String = "catch-skill"
    override val name: String = "Catch"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    // Catch is always available
    override val rerollResetAt: ResetPolicy = ResetPolicy.NEVER
    override val rerollDescription: String = "Catch Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*, *>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.CATCH
    }

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = CatchSkill(isTemporary, expiresAt)
    }
}
