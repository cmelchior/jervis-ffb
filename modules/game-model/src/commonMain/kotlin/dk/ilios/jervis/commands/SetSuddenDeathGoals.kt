package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class SetSuddenDeathGoals(private val team: Team, private val goals: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game, controller: GameController) {
        if (team.isHomeTeam()) {
            originalValue = state.homeSuddenDeathGoals
            state.homeSuddenDeathGoals = goals
        } else {
            originalValue = state.awaySuddenDeathGoals
            state.awaySuddenDeathGoals = goals
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (team.isHomeTeam()) {
            state.homeSuddenDeathGoals = originalValue
        } else {
            state.awaySuddenDeathGoals = originalValue
        }
    }
}
