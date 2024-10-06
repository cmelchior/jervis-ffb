package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.UseProReroll
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Pro(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, RerollSource {
    override val skillId: String = "pro-skill"
    override val id: RerollSourceId =
        RerollSourceId("pro-reroll")
    override val name: String = "Pro"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.GENERAL
    override var used: Boolean = false
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = Pro(isTemporary, expiresAt)
    }

    override val rerollResetAt: Duration = Duration.END_OF_ACTIVATION
    override val rerollDescription: String = "Pro Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseProReroll

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        TODO("Not yet implemented")
    }

    override fun calculateRerollOptions(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): List<DiceRerollOption> {


        TODO("Not yet implemented")
    }
}
