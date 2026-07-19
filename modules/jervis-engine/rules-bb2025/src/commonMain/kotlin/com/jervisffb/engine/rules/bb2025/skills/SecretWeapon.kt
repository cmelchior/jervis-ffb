package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Represents the "Secret Weapon* (Passive)" skill.
 *
 * See page XXX in the BB2025 rulebook.
 */
class SecretWeapon(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.TRAITS,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.SECRET_WEAPON
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.PASSIVE)

    // Secret Weapon only triggers if the player was on the pitch during the drive.
    // We track this here. It reset during "Deal With Secret Weapons".
    // We do not want to use `used` as it is rendered in a special way in the UI.
    var onPitchDuringDrive: Boolean = false
}
