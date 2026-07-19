package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.SpecialActionProvider

class Stab(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill, SpecialActionProvider {
    override val type: SkillType = SkillType.STAB
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val specialAction: PlayerSpecialActionType = PlayerSpecialActionType.STAB
    override var isSpecialActionUsed: Boolean = false
    override fun isActionAvailable(state: Game, rules: Rules): Boolean {
        return false
    }
}
