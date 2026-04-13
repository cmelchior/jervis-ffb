package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.common.rerolls.TeamMascotReroll

/**
 * Class representing a `Team Mascot` inducement.
 * The mascot itself doesn't have any state, as it cannot be "used". The reroll
 * itself is controlled through [TeamMascotReroll].
 *
 * See page 144 in the BB2025 rulebook.
 */
class TeamMascot(team: TeamId) {
    val reroll = TeamMascotReroll(team)
}
