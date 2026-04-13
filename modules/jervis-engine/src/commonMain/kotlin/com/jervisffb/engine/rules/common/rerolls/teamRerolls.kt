package com.jervisffb.engine.rules.common.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.rerolls.UseTeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource

/**
 * Interface describing all types of a Team Reroll.
 */
sealed interface TeamReroll : RerollSource {
    val teamId: TeamId
    // Whether this reroll can be carried over into overtime if it isn't used in the half
    val carryOverIntoOvertime: Boolean
    // When is this reroll removed from the Team, regardless of it being used or not
    val duration: Duration
    override val rerollProcedure: Procedure
        get() = UseTeamReroll

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        dicePool: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        if (rerollUsed) return false
        if (state.activeTeam?.id != teamId) return false
        if (state.activeTeam?.usedRerollThisTurn == true && !state.rules.allowMultipleTeamRerollsPrTurn) return false
        if (state.rules.canBeRerolledByTeamReroll(type)) return false
        return dicePool.all { it.rerollSource == null }
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this.id, value))
    }
}

/**
 * Class representing a regular team reroll that are part of the roster.
 */
class RegularTeamReroll(override val teamId: TeamId, val index: Int) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

/**
 * Class representing the reroll gained by having a player with Leader on the
 * team.
 *
 * Note, the availability of this reroll is determined by more complex rules.
 * These rules are handled in the relevant procedures.
 */
class LeaderTeamReroll(override val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-leader")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

/**
 * Class representing the reroll gained by rolling Brilliant Coaching on the
 * Kick-off Event Table.
 */
class BrilliantCoachingReroll(override val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-brilliant-coaching")
    override val carryOverIntoOvertime: Boolean = false // Because it only last for the current Drive
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_DRIVE
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
}

/**
 * Class representing the reroll provided by the Team Mascot inducement
 */
class TeamMascotReroll(override val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-mascot")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.END_OF_GAME
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Mascot)"
    override var rerollUsed: Boolean = false

    companion object {
        val TARGET: Int = 4
    }
}
