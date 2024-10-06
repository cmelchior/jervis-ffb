package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class ProjectileVomit(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, SpecialActionProvider {
    override val skillId: String = "projectile-vomit-skill"
    override val name: String = "Projectile Vomit"
    override val compulsory: Boolean = false
    override val resetAt: Duration =
        Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.TRAITS
    override var used: Boolean = false // This skill is always available
    override val value: Int? = null // Skill has no value
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val specialAction = PlayerSpecialActionType.PROJECTILE_VOMIT
    override var isSpecialActionUsed: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill =
            ProjectileVomit(isTemporary, expiresAt)
    }

}
