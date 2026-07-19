package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class AddTouchdown(private val team: Team, private val touchdowns: Int) : Command {
    private var originalValue: Int = -1
    override fun execute(state: Game) {
        if (team.isHomeTeam()) {
            originalValue = state.homeTouchdowns
            state.homeTouchdowns += touchdowns
        } else {
            originalValue = state.awayTouchdowns
            state.awayTouchdowns += touchdowns
        }
    }

    override fun undo(state: Game) {
        if (team.isHomeTeam()) {
            state.homeTouchdowns = originalValue
        } else {
            state.awayTouchdowns = originalValue
        }
    }
}
