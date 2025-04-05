package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Blood Lust(X+)* skill.
 *
 * See Spike Magazin XXX
 */
@Serializable
class BloodLust(
    override val skillId: SkillId,
    override val value: Int,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val name: String = "Blood Lust"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    class Factory(override val value: Int): PlayerSkillFactory {
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill = BloodLust(
            SkillId("${player.id.value}-Bloodlust"),
            value,
            isTemporary,
            expiresAt
        )
    }
}
