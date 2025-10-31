package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the Hatred(X) skill. If a player gets hatred against
 * multiple opponents, this is represented by multiple instances of this skill.
 *
 * See page xx in the BB2025 rulebook.
 */
class Hatred(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val value: PlayerKeyword?,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025KeywordSkill {
    override val type: SkillType = SkillType.HATRED
    override val skillId: SkillId = type.id(value)
    override val name: String = "${type.description}(${value?.description ?: "None"})"
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
}
