package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team

class AddGoal(private val team: Team, private val goals: Int) : Command {
    private var originalValue: Int = -1
    override fun execute(state: Game, controller: GameController) {
        if (team.isHomeTeam()) {
            originalValue = state.homeGoals
            state.homeGoals += goals
        } else {
            originalValue = state.awayGoals
            state.awayGoals += goals
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (team.isHomeTeam()) {
            state.homeGoals = originalValue
        } else {
            state.awayGoals = originalValue
        }
    }
}
