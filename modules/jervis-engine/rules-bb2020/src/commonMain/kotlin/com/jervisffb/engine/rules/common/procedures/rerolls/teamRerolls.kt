package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Duration

sealed interface BB2020TeamReroll: TeamReroll {
    override val rerollProcedure: Procedure
        get() = BB2020UseTeamReroll
}

/**
 * Class representing a regular team reroll that are part of the roster.
 */
class BB2020StandardTeamReroll(override val teamId: TeamId, val index: Int) : BB2020TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}

/**
 * Class representing the reroll gained by rolling Brilliant Coaching on the
 * Kick-off Event Table.
 */
class BB2020BrilliantCoachingReroll(override val teamId: TeamId) : BB2020TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-brilliant-coaching")
    override val carryOverIntoOvertime: Boolean = false // Because it only last for the current Drive
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_DRIVE
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
    override var enabled: Boolean = true
}
