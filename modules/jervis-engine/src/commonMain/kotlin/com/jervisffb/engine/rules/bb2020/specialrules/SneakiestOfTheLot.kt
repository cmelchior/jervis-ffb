package com.jervisffb.engine.rules.bb2020.specialrules

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Sneakiest of the Lot special rule.
 *
 * See rulebook page 129.
 */
@Serializable
class SneakiestOfTheLot(
    override val skillId: SkillId
) : BB2020SpecialRule {
    override val isTemporary: Boolean = false
    override val expiresAt: Duration = Duration.PERMANENT
    override val name: String = "Sneakiest of the Lot"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.END_OF_TURN
    override val category: SkillCategory = BB2020SkillCategory.SPECIAL_RULES
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true

    @Serializable
    data object Factory: SpecialRuleFactory {
        override val value: Int? = null
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill {
            return SneakiestOfTheLot(
                SkillId("${player.id}-SneakiestOfTheLot"),
            )
        }
    }
}
