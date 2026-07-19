package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider

/**
 * Representation of the "Chainsaw* (Active)" skill.
 *
 * See page 126 in the BB2025 rulebook.
 */
class Chainsaw(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, SpecialActionProvider {
    override val type: SkillType = SkillType.CHAINSAW
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    // The rulebook defines Chainsaw as "mandatory", but the consensus is that it only applies to the +3 on AV rolls
    // and not to the action itself. I.e. a Chainsaw player can still choose to use a Move Action, and is not just
    // restricted to Block and Blitz.
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val specialAction = PlayerSpecialActionType.CHAINSAW
    override var isSpecialActionUsed: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
    override fun isActionAvailable(state: Game, rules: Rules): Boolean {
        return isSkillAvailableAndAdjacentToOpponent(player, type, state, rules)
    }
}
