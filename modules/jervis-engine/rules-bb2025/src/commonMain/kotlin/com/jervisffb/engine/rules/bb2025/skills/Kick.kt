package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.PuntContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.D6StandardSkillReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Kick" skill.
 *
 * See page 130 in the BB2025 rulebook.
 */
class Kick(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.GENERAL,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, D6StandardSkillReroll {
    override val type: SkillType = SkillType.KICK
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.serialize()}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    // Kick can be used to reroll Punt
    override val rerollDescription: String = "Kick Reroll"
    override val rerollResetAt: Duration = Duration.END_OF_ACTION
    override var rerollUsed: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override fun canReroll(state: Game, type: DiceRollType, dicePool: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        if (rerollUsed) return false
        val context = state.getContextOrNull<PuntContext>() ?: return false
        if (player != context.punter) return false
        return when (type) {
            DiceRollType.PUNT_DIRECTION,
            DiceRollType.PUNT_DISTANCE -> true
            else -> false
        }
    }
}
