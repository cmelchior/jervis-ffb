package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import kotlinx.serialization.Serializable

@Serializable
class Dodge(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, D6StandardSkillReroll {
    override val skillId: String = "dodge-skill"
    override val id: RerollSourceId = RerollSourceId("dodge-reroll")
    override val name: String = "Dodge"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    override val rerollDescription: String = "Dodge Reroll"
    override val rerollResetAt: Duration = Duration.END_OF_TURN
    override var rerollUsed: Boolean = false

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        return type == DiceRollType.DODGE
    }

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            Dodge(isTemporary, expiresAt)
    }
}
