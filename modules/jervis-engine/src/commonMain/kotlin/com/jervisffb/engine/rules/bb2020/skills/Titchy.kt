package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Titchy* skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class Titchy(
    override val skillId: SkillId,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val name: String = "Titchy"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: PlayerSkillFactory {
        override val value: Int? = null
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill {
            return Titchy(
                SkillId("${player.id.value}-Titchy"),
                isTemporary,
                expiresAt
            )
        }
    }
}
