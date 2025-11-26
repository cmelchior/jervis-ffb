package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.D6StandardSkillReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the Sure Feet (Active) skill.
 *
 * See page 136 in the BB2025 rulebook.
 */
class SureFeet(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.AGILITY,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, D6StandardSkillReroll {
    override val type: SkillType = SkillType.SURE_FEET
    override val value: Unit? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.serialize()}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override val rerollResetAt: Duration = Duration.END_OF_TURN
    override val rerollDescription: String = "Sure Feet Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.RUSH
    }
}
