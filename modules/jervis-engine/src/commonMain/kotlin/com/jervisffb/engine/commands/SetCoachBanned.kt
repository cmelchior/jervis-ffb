package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetCoachBanned(private val team: Team, private val banned: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = team.coachBanned
        team.coachBanned = banned
        team.notifyUpdate()
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.coachBanned = originalValue
        team.notifyUpdate()
    }
}
