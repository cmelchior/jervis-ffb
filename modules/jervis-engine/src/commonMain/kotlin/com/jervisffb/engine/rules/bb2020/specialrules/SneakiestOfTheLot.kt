package com.jervisffb.engine.rules.bb2020.specialrules

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillType

/**
 * Representation of the Sneakiest of the Lot special rule.
 *
 * See rulebook page 129.
 */
class SneakiestOfTheLot(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.SPECIAL_RULES,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020SpecialRule {
    override val type: SkillType = SkillType.SNEAKIEST_OF_THE_LOT
    override val value: Int? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.END_OF_TURN
    override var used: Boolean = false
        get() = TODO("Not yet implemented")
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
}
