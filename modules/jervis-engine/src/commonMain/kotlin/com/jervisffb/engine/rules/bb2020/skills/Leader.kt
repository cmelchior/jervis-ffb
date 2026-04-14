package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveTeamReroll
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.common.rerolls.LeaderTeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Representation of the Leader skill. See page 79 in the rulebook.
 *
 * The skill itself has no effect by itself. It is just a marker used by the rules.
 *
 * It isn't defined when the Leader reroll is actually added, just that
 * "..with this skill gains a single extra team-reroll', and Master Chef
 * says that it is rolled in the Start of Drive Sequence (and mention that it
 * doesn't work for Leader rerolls). This could imply it isn't added until after
 * Master Chef has rolled.
 *
 * This leaves some options on when to add the reroll, and the choice seems
 * somewhat arbitrary:
 *
 * 1. Add it at the beginning of the game, and then just toggle the "used" state
 *    depending on whether it is available or not.
 * 2. Add it after Master Chef has rolled.
 * 3. Add/Remove it during setup as the players are moved in/out of the field
 * 4. Add it after the setup is complete.
 *
 * Adding it after Master Chef felt confusing, so 4) was chosen. This choice
 * probably needs user feedback to settle.
 *
 * Leader is removed when a player leaves the field. Which can happen through
 * in a number of places. So far the following cases have been identified:
 *
 * - (✓) During setup: We only check for leader after the setup is complete.
 * - (✓) Knocked down: Reroll is present until player has finalized any injury.
 * - (✓) Falling over: Reroll is present until player has finalized any injury.
 * - (✓) Pushed into the crowd: Reroll is removed as soon as the player leaves the
 *   field. I.e., as soon as the player is physically moved.
 * - (✓) Sent-off: Reroll is available only after argue the call has resolved.
 *
 * Open question: Are there any special effects that can remove a player from
 * the field without having them Knocked Down or Falling Over?
 */
class Leader(
    override val player: Player,
    override val category: SkillCategory = SkillCategory.PASSING,
    override val expiresAt: Duration = Duration.PERMANENT,
) : BB2020Skill {
    override val type: SkillType = SkillType.LEADER
    override val value: Unit? = null
    override val skillId: SkillId = type.id(value)
    override val name: String = type.description
    override val compulsory: Boolean = false
    override val resetAt: Duration = Duration.PERMANENT
    override var used: Boolean = false
    override val workWithoutTackleZones: Boolean = false
    override val workWhenProne: Boolean = false

    companion object {
        /**
         * Runs through all players on the field, and if no player is found with
         * the Leader skill, this method returns the commands needed to remove
         * Rerolls no longer available.
         */
        fun removeLeaderRerollIfNotAvailable(team: Team): Command? {
            val rules = team.game.rules
            val leaderOnField = team.any { it.location.isOnField(rules) && it.hasSkill(SkillType.LEADER) }
            return if (!leaderOnField) {
                val commands = team.rerolls.filterIsInstance<LeaderTeamReroll>().map {
                    RemoveTeamReroll(team, it)
                }
                compositeCommandOf(commands)
            } else {
                null
            }
        }
    }
}
