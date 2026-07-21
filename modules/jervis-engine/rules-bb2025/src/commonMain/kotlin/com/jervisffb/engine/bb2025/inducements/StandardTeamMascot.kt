package com.jervisffb.engine.bb2025.inducements

import com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotReroll
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.TeamMascot

/**
 * Class representing a `Team Mascot` inducement.
 * The mascot itself doesn't have any state, as it cannot be "used". The reroll
 * itself is controlled through [TeamMascotReroll].
 *
 * See page 144 in the BB2025 rulebook.
 */
class StandardTeamMascot(team: TeamId): TeamMascot {
    override val reroll = TeamMascotReroll(team)
}
