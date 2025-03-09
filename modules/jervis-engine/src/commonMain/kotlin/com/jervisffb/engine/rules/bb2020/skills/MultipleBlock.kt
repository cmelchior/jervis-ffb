package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class MultipleBlock(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, SpecialActionProvider {
    override val skillId: String = "multiple-block-skill"
    override val name: String = "Multiple Block"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.STRENGTH
    override var used: Boolean = false
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val specialAction = PlayerSpecialActionType.MULTIPLE_BLOCK
    override var isSpecialActionUsed: Boolean = false

    @Serializable
    data object Factory: PlayerSkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = MultipleBlock(isTemporary, expiresAt)
    }

}
