package dk.ilios.jervis.rules.skills

import dk.ilios.jervis.procedures.DieRoll
import dk.ilios.jervis.rules.bb2020.BB2020SkillCategory
import kotlinx.serialization.Serializable

@Serializable
class SureFeet(
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, D6StandardSkillReroll {
    override val skillId: String = "sure-feet-skill"
    override val id: RerollSourceId = RerollSourceId("sure-feet-reroll")
    override val name: String = "Sure Feet"
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    override val rerollResetAt: Duration = Duration.END_OF_TURN
    override val rerollDescription: String = "Sure Feet Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        return type == DiceRollType.RUSH
    }

    @Serializable
    data object Factory: SkillFactory {
        override val value: Int? = null
        override fun createSkill(isTemporary: Boolean, expiresAt: Duration): Skill = SureFeet(isTemporary, expiresAt)
    }
}
