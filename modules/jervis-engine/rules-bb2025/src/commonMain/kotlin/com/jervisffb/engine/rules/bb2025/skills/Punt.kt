package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider

/**
 * Represents the "Punt" skill.
 *
 * See page 135 in the BB2025 rulebook.
 */
class Punt(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.PASSING,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill, SpecialActionProvider {
    override val type: SkillType = SkillType.PUNT
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
    override val specialAction: PlayerSpecialActionType = PlayerSpecialActionType.PUNT
    override var isSpecialActionUsed: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    override fun isActionAvailable(state: Game, rules: Rules): Boolean {
        if (!player.location.isOnPitch(rules)) return false
        val actionsAvailable = state.activeTeamOrThrow().turnData.availableSpecialActions.getOrElse(specialAction) { 0 }
        return player.isSkillAvailable(type) && !isSpecialActionUsed && actionsAvailable > 0
    }
}
