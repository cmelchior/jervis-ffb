package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import kotlinx.serialization.Serializable

@Serializable
class CatchSkill(
    override val skillId: SkillId,
    override val isTemporary: Boolean = false,
    override val expiresAt: Duration = Duration.PERMANENT
) : BB2020Skill, D6StandardSkillReroll {
    override val id: RerollSourceId = RerollSourceId("${skillId.value}-reroll")
    override val name: String = "Catch"
    override val compulsory: Boolean = false
    override val resetAt: Duration =Duration.PERMANENT
    override val category: SkillCategory = BB2020SkillCategory.AGILITY
    override var used: Boolean = false
    override val value: Int? = null
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
        return type == DiceRollType.CATCH
    }

    @Serializable
    data object Factory: PlayerSkillFactory {
        override val value: Int? = null
        override fun createSkill(
            player: Player,
            isTemporary: Boolean,
            expiresAt: Duration
        ): Skill {
            return CatchSkill(
                SkillId("${player.id.value}-Catch"),
                isTemporary,
                expiresAt
            )
        }
    }
}
