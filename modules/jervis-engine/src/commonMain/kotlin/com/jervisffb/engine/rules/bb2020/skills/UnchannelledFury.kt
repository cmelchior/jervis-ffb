package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Unchannelled Fury* skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class UnchannelledFury(
    override val skillId: SkillId,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val name: String = "Unchannelled Fury"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    data object Factory: PlayerSkillFactory {
        override val value: Int? = null
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill {
            return UnchannelledFury(
                SkillId("${player.id.value}-UnchannelledFury"),
                isTemporary,
                expiresAt
            )
        }
    }
}
