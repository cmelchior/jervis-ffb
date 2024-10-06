package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class MightyBlow(
    override val value: Int = 1,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill {
    override val skillId: String = "mighty-blow-skill"
    override val name: String = "Mighty Blow ($value+)"
    override val compulsory: Boolean = false
    override val resetAt: Duration =
        Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.STRENGTH
    override var used: Boolean = false // This skill is always available
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data class Factory(override val value: Int): SkillFactory {
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            MightyBlow(value, isTemporary, expiresAt)
    }
}
