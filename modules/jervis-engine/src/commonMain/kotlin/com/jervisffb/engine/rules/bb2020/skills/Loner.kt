package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class Loner(
    override val skillId: SkillId,
    override val value: Int,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val name: String = "Loner ($value+)"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data class Factory(override val value: Int): PlayerSkillFactory {
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill {
            return Loner(
                SkillId("${player.id.value}-Loner"),
                value,
                isTemporary,
                expiresAt
            )
        }
    }
}
