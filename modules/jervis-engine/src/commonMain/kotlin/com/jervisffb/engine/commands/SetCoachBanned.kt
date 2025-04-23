package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetCoachBanned(private val team: Team, private val banned: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(
        state: Game,
    ) {
        originalValue = team.coachBanned
        team.coachBanned = banned
    }

    override fun undo(
        state: Game,
    ) {
        team.coachBanned = originalValue
    }
}
