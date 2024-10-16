package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetSuddenDeathGoals(private val team: Team, private val goals: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        if (team.isHomeTeam()) {
            originalValue = state.homeSuddenDeathGoals
            state.homeSuddenDeathGoals = goals
        } else {
            originalValue = state.awaySuddenDeathGoals
            state.awaySuddenDeathGoals = goals
        }
    }

    override fun undo(state: Game) {
        if (team.isHomeTeam()) {
            state.homeSuddenDeathGoals = originalValue
        } else {
            state.awaySuddenDeathGoals = originalValue
        }
    }
}
