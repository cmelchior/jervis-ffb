package com.jervisffb.engine.rules.bb2025.skills

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveTeamReroll
import com.jervisffb.engine.commands.SetTeamRerollEnabled
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.SkillKeyword
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.common.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Representation of the "Leader (Passive)" skill. See page 130 in the BB2025
 * rulebook.
 *
 * The skill itself has no effect by itself. It is just a marker used by the
 * rules.
 *
 * Developer's Commentary:
 * The rules concerning the life-cycle of the Leader re-roll are a bit
 * complicated. See the full discussion in
 * [BB2025 Skills](../website/bb2025/bb2025-skills.md).
 *
 * But Jervis uses the following logic for Leader:
 *
 * 1. During the first Start of Drive sequence in both halfs, we add a Leader
 *    reroll if the Leader is placed on the Pitch.
 *
 *    If the Leader is not placed on the Pitch here, the re-roll is not
 *    added in a later Drive in the same half.
 *
 * 2. During the half. If the Leader leaves the Pitch, the re-roll is
 *    disabled immediately, if no other Leaders are present.  If all Leaders are
 *    marked as "Removed from Play", the re-roll is completely removed.
 *
 * 3. If the Leader leaves the Pitch, and is later returned, e.g. when setting
 *    up a new Drive, the re-roll is re-enabled (if not already used).
 *
 * 4. A Leader re-roll added in the last half, carry over into Extra Time, but
 *    will start disabled if the Leader isn't placed in the first Drive. It can
 *    be re-enabled if the Leader is returned to the Pitch during Extra Time.
 */
class Leader(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.PASSING,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2025Skill {
    override val type: SkillType = SkillType.LEADER
    override val value: Unit? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = true
    override val workWhenProne: Boolean = true
    override val keywords: List<SkillKeyword> = listOf(SkillKeyword.PASSIVE)

    companion object {
        /**
         * If a Leader was removed from the Pitch, determine what should happen
         * with the Leader re-roll. 3 options exist:
         *
         * 1. Leader was removed from the Pitch, but another Leader remains. Nothing happens.
         * 2. Leader was removed from the pitch (temporarily). Reroll is disabled.
         * 3. Leader was removed (permanently). Reroll is removed.
         */
        fun calculateLeaderRerollStatusChange(team: Team): Command? {
            val reroll = team.rerolls.singleOrNull { it is LeaderTeamReroll } ?: return null
            val rules = team.game.rules
            val leaders = team.filter { it.hasSkill(SkillType.LEADER) }
            val leaderOnPitch = leaders.any { it.location.isOnPitch(rules) && it.hasSkill(SkillType.LEADER) }
            val leadersPermanentlyRemoved = leaders
                .all { player ->
                    val isInDogout = (player.location == DogOut)
                    val removedState = when (player.state) {
                        PlayerState.RESERVE,
                        PlayerState.KNOCKED_OUT,
                        PlayerState.DEAD,
                        PlayerState.FAINTED,
                        PlayerState.DODGY_SNACK,
                        PlayerState.STANDING,
                        PlayerState.PRONE,
                        PlayerState.STUNNED,
                        PlayerState.STUNNED_OWN_TURN -> false

                        PlayerState.BADLY_HURT,
                        PlayerState.LASTING_INJURY,
                        PlayerState.SERIOUSLY_HURT,
                        PlayerState.SERIOUS_INJURY,
                        PlayerState.BANNED -> true
                    }
                    isInDogout && removedState
                }
            return when {
                leaderOnPitch -> null
                !leaderOnPitch && !leadersPermanentlyRemoved -> SetTeamRerollEnabled(team, reroll, enabled = false)
                !leaderOnPitch && leadersPermanentlyRemoved -> RemoveTeamReroll(team, reroll)
                else -> INVALID_GAME_STATE("Unexpected leader state: ($leaderOnPitch, $leadersPermanentlyRemoved)")
            }
        }
    }
}
