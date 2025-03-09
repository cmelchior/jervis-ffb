package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import kotlinx.serialization.Serializable

@Serializable
class SureHands(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, D6StandardSkillReroll {
    override val skillId: String = "surehands-skill"
    override val id: RerollSourceId =
        RerollSourceId("sure-hands-reroll")
    override val name: String = "Sure Hands"
    override val compulsory: Boolean = false
    override val resetAt: Duration =
        Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.GENERAL
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    // Sure Hands is always available
    override val rerollResetAt: Duration =
        Duration.PERMANENT
    override val rerollDescription: String = "Sure Hands Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.PICKUP
    }

    @Serializable
    data object Factory: PlayerSkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            SureHands(isTemporary, expiresAt)
    }
}
