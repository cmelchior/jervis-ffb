package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Regeneration(
    override val skillId: SkillId,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val name: String = "Regeneration"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false // This skill is always available
    override val value: Int? = null // Skill has no value
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
            return Regeneration(
                SkillId("${player.id.value}-Regeneration"),
                isTemporary,
                expiresAt
            )
        }
    }
}
