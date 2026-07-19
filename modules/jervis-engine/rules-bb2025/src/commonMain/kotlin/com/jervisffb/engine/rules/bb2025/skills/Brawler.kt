package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.UseBrawlerReroll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.anyRerollUsed

/**
 * Represents the "Brawler (Active)" skill.
 *
 * See page 129 in the BB2025 rulebook.
 */
class Brawler(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.STRENGTH,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, RerollSource {
    override val type: SkillType = SkillType.BRAWLER
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.serialize()}-reroll")
    override val rerollResetAt: Duration = Duration.END_OF_ACTION
    override val rerollDescription: String = "Brawler Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseBrawlerReroll

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        dicePool: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): Boolean {
        if (type != DiceRollType.BLOCK) return false
        if (rerollUsed) return false
        if (dicePool.anyRerollUsed() && !state.rules.canUseMultipleRerollsOnDicePools) return false

        @Suppress("UNCHECKED_CAST")
        val diceRolls = dicePool as List<DieRoll<DBlockResult>>

        // Only works if no other dice have been re-rolled and a both-down result exists
        val isBlockingPlayer = (state.getContextOrNull<BlockContext>()?.attacker == player)

        val context = state.getContextOrNull<ActivatePlayerContext>()
        val isDeclaredBlockAction = (context?.declaredAction?.type == PlayerStandardActionType.BLOCK)

        val hasRerollableResult = diceRolls.any  {
            it.rerollSource == null && it.result.blockResult == BlockDice.BOTH_DOWN
        }

        return isDeclaredBlockAction && isBlockingPlayer && hasRerollableResult
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): List<DiceRerollOption> {
        @Suppress("UNCHECKED_CAST")
        val diceRolls = value as List<DieRoll<DBlockResult>>
        return diceRolls
            .firstOrNull { it.result.blockResult == BlockDice.BOTH_DOWN }
            ?.let { _ ->
                listOf(DiceRerollOption(this.id, null))
            } ?: INVALID_GAME_STATE("No Both Down results were found: $diceRolls")
    }
}
