package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.modifiers.DefensiveAssistsArmourModifier
import com.jervisffb.engine.model.modifiers.OffensiveAssistArmourModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.UseLonerReroll
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Lone Fouler (Active)" skill.
 *
 * See page 130 in the BB2025 rulebook.
 */
class LoneFouler(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.DEVIOUS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, RerollSource {
    override val type: SkillType = SkillType.LONE_FOULER
    override val value: Unit? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.END_OF_ACTION
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override val id: RerollSourceId = RerollSourceId("${player.id.value}-lone-fouler")
    override val rerollResetAt: Duration = Duration.END_OF_ACTION
    override val rerollDescription: String = "Lone Fouler Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseLonerReroll

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): Boolean {
        if (type != DiceRollType.ARMOUR) return false
        if (rerollUsed) return false
        return state.getContextOrNull<RiskingInjuryContext>()?.let { context ->
            val armourBroken = context.armourBroken
            val hasDefensiveAssists = context.armourModifiers.any { it is DefensiveAssistsArmourModifier }
            val hasOffensiveAssists = context.armourModifiers.any { it is OffensiveAssistArmourModifier }
            !armourBroken && !hasDefensiveAssists && !hasOffensiveAssists
        } ?: false
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this.id, value))
    }
}
