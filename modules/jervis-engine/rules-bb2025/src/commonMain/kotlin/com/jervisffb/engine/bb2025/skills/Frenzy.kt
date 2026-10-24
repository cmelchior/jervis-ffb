package com.jervisffb.engine.bb2025.skills

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.utils.endActionImmediately
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the Frenzy* (Active) skill.
 *
 * See page 128 in the BB2025 rulebook.
 */
class Frenzy(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.GENERAL,
    override val expiresAt: Duration = Duration.PERMANENT,
) : Skill2025 {
    override val type: SkillType = SkillType.FRENZY
    override val value: Unit? = null
    override val skillId: SkillId = type.id()
    override val name: String = type.description
    override val compulsory: Boolean = true
    override val resetAt: Duration = Duration.END_OF_ACTIVATION
    override var used: Boolean = false // Whether the second Block has been triggered.
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.ACTIVE)

    // Follow-up remains compulsory on the second Block, even after `used` is set.
    fun forcesFollowUp(state: Game): Boolean {
        return state.rules.isStanding(player) && player.hasTackleZones && !state.rules.isDistracted(player)
    }

    /**
     * Check whether a completed standard Block requires a second Frenzy Block.
     * A push result also qualifies when Stand Firm, Rooted or Chomped prevents
     * the defender from moving. See page 128 in the BB2025 rulebook and the
     * Frenzy commentary in `website/bb2025/bb2025-skills.md`.
     */
    fun mustPerformSecondBlock(state: Game): Boolean {
        val block = state.getContextOrNull<BlockContext>() ?: return false
        if (block.aborted || state.endActionImmediately() || !player.isSkillAvailable(type)) return false
        val hasPushResult = when (block.result.blockResult) {
            BlockDice.PUSH_BACK,
            BlockDice.STUMBLE,
            BlockDice.POW -> true
            BlockDice.PLAYER_DOWN,
            BlockDice.BOTH_DOWN -> false
        }
        return hasPushResult
            && state.rules.isStanding(player)
            && state.rules.isStanding(block.defender)
            && player.coordinates.getSurroundingCoordinates(state.rules).contains(block.defender.coordinates)
    }
}
