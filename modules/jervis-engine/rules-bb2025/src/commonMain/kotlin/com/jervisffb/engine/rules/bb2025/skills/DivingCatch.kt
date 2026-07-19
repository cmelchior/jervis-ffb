package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the "Diving Catch (Active)" skill.
 *
 * See page 127 in the BB2025 rulebook.
 */
class DivingCatch(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.AGILITY,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.DIVING_CATCH
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = false
    // We mark Diving Catch as used when resolving a ball landing. The status
    // is reset after resolving the ball. This is needed as this might be used
    // during a Kick-off Event, which would have a different Duration Even than
    // "Activaton" or "Action"
    override val resetAt: Duration = Duration.SPECIAL
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)
}
