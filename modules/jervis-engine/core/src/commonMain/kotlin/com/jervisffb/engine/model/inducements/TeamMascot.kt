package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.rules.common.rerolls.TeamReroll

/** Interface representing a `Team Mascot` inducement. */
interface TeamMascot {
    // Reference to the reroll representing the team mascot being used as one.
    val reroll: TeamReroll
}
