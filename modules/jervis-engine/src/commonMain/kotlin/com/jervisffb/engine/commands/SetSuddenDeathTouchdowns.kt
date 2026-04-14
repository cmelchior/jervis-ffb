package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetSuddenDeathTouchdowns(private val team: Team, private val touchdowns: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        if (team.isHomeTeam()) {
            originalValue = state.homeSuddenDeathTouchdowns
            state.homeSuddenDeathTouchdowns = touchdowns
        } else {
            originalValue = state.awaySuddenDeathTouchdowns
            state.awaySuddenDeathTouchdowns = touchdowns
        }
    }

    override fun undo(state: Game) {
        if (team.isHomeTeam()) {
            state.homeSuddenDeathTouchdowns = originalValue
        } else {
            state.awaySuddenDeathTouchdowns = originalValue
        }
    }
}
