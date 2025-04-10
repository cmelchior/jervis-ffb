package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.UseProReroll

class Pro(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.GENERAL,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill, RerollSource {
    override val type: SkillType = SkillType.LEAP
    override val value: Int? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val id: RerollSourceId = RerollSourceId("${player.id.value}-${skillId.value}-reroll")
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    override val rerollResetAt: Duration = Duration.END_OF_ACTIVATION
    override val rerollDescription: String = "Pro Reroll"
    override var rerollUsed: Boolean = false
    override val rerollProcedure: Procedure = UseProReroll

    override fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): Boolean {
        return false
        // TODO("Not yet implemented")
    }

    override fun calculateRerollOptions(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean?): List<DiceRerollOption> {
        return emptyList()
        // TODO("Not yet implemented")
    }
}
