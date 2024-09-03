package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.procedures.UseProReroll
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Pro(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill, RerollSource  {
    override val id: String = "pro-skill"
    override val name: String = "Pro"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.NEVER
    override val category: SkillCategory = BB2020SkillCategory.GENERAL
    override var used: Boolean = false
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = Pro(isTemporary, expiresAt)
    }

    override val rerollResetAt: ResetPolicy = ResetPolicy.END_OF_ACTIVATION
    override val rerollDescription: String = "Pro Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseProReroll

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*, *>>, wasSuccess: Boolean?): Boolean {
        TODO("Not yet implemented")
    }

    override fun calculateRerollOptions(type: DiceRollType, value: List<DieRoll<*, *>>, wasSuccess: Boolean?): List<DiceRerollOption> {
        TODO("Not yet implemented")
    }
}
