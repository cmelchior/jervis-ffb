package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.D6StandardSkillReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the "Dodge (Active)" skill.
 *
 * See page 127 in the BB2025 rulebook.
 */
class Dodge(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.AGILITY,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, D6StandardSkillReroll {
    override val type: SkillType = SkillType.DODGE
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.serialize()}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val rerollDescription: String = "Dodge Reroll"
    override val rerollResetAt: Duration = Duration.END_OF_TURN
    override var rerollUsed: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(
        SkillKeyword.ACTIVE,
        SkillKeyword.ELITE,
    )

    override fun canReroll(state: Game, type: DiceRollType, dicePool: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        if (rerollUsed) return false
        val context = state.getContextOrNull<DodgeRollContext>()
        return (type == DiceRollType.DODGE) && (context?.useTackle == null)
    }
}
