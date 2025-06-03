package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll

/**
 * Representation of the Catch skill.
 *
 * See the rulebook, page 75.
 */
class CatchSkill(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.AGILITY,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill, D6StandardSkillReroll {
    override val type: SkillType = SkillType.CATCH
    override val value: Int? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.toPrettyString()}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration =Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    // Catch is always available
    override val rerollResetAt: Duration = Duration.PERMANENT
    override val rerollDescription: String = "Catch Reroll"
    override var rerollUsed: Boolean = false

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        // Catch only allows rerolling failed rolls
        return type == DiceRollType.CATCH && wasSuccess == false && player.hasTackleZones
    }
}
