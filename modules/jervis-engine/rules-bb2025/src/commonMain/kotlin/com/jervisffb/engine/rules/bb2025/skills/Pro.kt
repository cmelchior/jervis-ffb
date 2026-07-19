package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.UseProReroll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.anyRerollUsed

/**
 * Representation of the Pro (Active) skill.
 *
 * See page 133 in the BB2025 rulebook.
 */
class Pro(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.GENERAL,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, RerollSource {
    val successTarget = 3 // What to roll to succeed
    override val type: SkillType = SkillType.PRO
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.serialize()}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.END_OF_ACTIVATION
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override val rerollResetAt: Duration = Duration.END_OF_ACTIVATION
    override val rerollDescription: String = "Pro Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseProReroll

    override fun canReroll(state: Game, type: DiceRollType, dicePool: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        if (rerollUsed) return false
        if (state.activePlayer != player) return false
        if (dicePool.anyRerollUsed() && !state.rules.canUseMultipleRerollsOnDicePools) return false

        // It is a bit unclear if Pro can re-roll a different set of dice than a Team re-roll.
        // For now, we assume the answer is mostly no. The activePlayer check will filter out
        // most candidates anyway (like Landing). Team Captain/Mascot is the exceptions so far.
        if (type == DiceRollType.TEAM_CAPTAIN) return false
        if (type == DiceRollType.TEAM_MASCOT) return false
        if (!state.rules.canBeRerolledByTeamReroll(type)) return false
        return true
    }

    override fun calculateRerollOptions(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): List<DiceRerollOption> {
        return value
            .filter { it.rerollSource == null }
            .map { die ->
                DiceRerollOption(id, die)
            }
    }
}
