package com.jervisffb.engine.bb2025.procedures.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Duration

sealed interface BB2025TeamReroll: TeamReroll {
    override val rerollProcedure: Procedure
        get() = UseTeamReroll
}

/**
 * Class representing a regular team reroll that are part of the roster.
 */
class StandardTeamReroll(override val teamId: TeamId, val index: Int) : BB2025TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}

/**
 * Class representing the reroll gained by having a player with Leader on the
 * team.
 *
 * Note, the availability of this reroll is determined by more complex rules.
 * These rules are handled in the relevant procedures.
 */
class LeaderTeamReroll(override val teamId: TeamId) : BB2025TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-leader")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}

/**
 * Class representing the reroll gained by rolling Brilliant Coaching on the
 * Kick-off Event Table.
 */
class BrilliantCoachingReroll(override val teamId: TeamId) : BB2025TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-brilliant-coaching")
    override val carryOverIntoOvertime: Boolean = false // Because it only last for the current Drive
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_DRIVE
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}

/**
 * Class representing the reroll provided by the Team Mascot inducement
 */
class TeamMascotReroll(override val teamId: TeamId) : BB2025TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-mascot")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.END_OF_GAME
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Mascot)"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true

    companion object {
        val TARGET: Int = 4
    }
}

/**
 * Class representing the reroll provided by the Extra Team Training inducement.
 */
class ExtraTeamTrainingReroll(override val teamId: TeamId, index: Int) : BB2025TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-extra-team-training-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.END_OF_GAME
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Extra Team Training)"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}
