package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.common.skills.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

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
    override val skillId: SkillId = type.id(value)
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
    override val rerollProcedure: Procedure = UseStandardSkillReroll

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): Boolean {
        if (type != DiceRollType.BLOCK) return false

        @Suppress("UNCHECKED_CAST")
        val diceRolls = value as List<DieRoll<DBlockResult>>

        // Only works if other have been re-rolled and a both-down result exists
        val isBlockingPlayer = (state.getContextOrNull<BlockContext>()?.attacker == player)
        val rerollAllowed = state.rules.isRerollAllowed(diceRolls)
        val hasRerollableResult = diceRolls.any  {
            it.rerollSource == null && it.result.blockResult == BlockDice.BOTH_DOWN
        }

        return isBlockingPlayer && rerollAllowed && hasRerollableResult
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): List<DiceRerollOption> {
        @Suppress("UNCHECKED_CAST")
        val diceRolls = value as List<DieRoll<DBlockResult>>
        return diceRolls
            .filter { it.result.blockResult == BlockDice.BOTH_DOWN }
            .map { die ->
                DiceRerollOption(this.id, die)
            }
    }
}
