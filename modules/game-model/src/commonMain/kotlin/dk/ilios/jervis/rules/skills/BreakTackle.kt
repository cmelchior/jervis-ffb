package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

/**
 * Representation of the Two Heads skill.
 *
 * See page 87 in the rulebook.
 */
@Serializable
class BreakTackle(
    override val isTemporary: Boolean = false,
    override val expiresAt: ResetPolicy = ResetPolicy.NEVER
) : BB2020Skill{
    override val id: String = "break-tackle-skill"
    override val name: String = "Break Tackle"
    override val compulsory: Boolean = false
    override val resetAt: ResetPolicy = ResetPolicy.END_OF_TURN
    override val category: SkillCategory = BB2020SkillCategory.STRENGTH
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: ResetPolicy): Skill = BreakTackle(isTemporary, expiresAt)
    }
}
