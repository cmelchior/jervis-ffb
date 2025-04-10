package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll

class Dodge(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.AGILITY,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill, D6StandardSkillReroll {
    override val type: SkillType = SkillType.DODGE
    override val value: Int? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.value}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val rerollDescription: String = "Dodge Reroll"
    override val rerollResetAt: Duration = Duration.END_OF_TURN
    override var rerollUsed: Boolean = false

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        return type == DiceRollType.DODGE
    }
}
