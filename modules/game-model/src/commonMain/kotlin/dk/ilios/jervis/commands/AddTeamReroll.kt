package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.skills.TeamReroll

/**
 * Add a new reroll to the team.
 */
class AddTeamReroll(private val team: Team, private val reroll: TeamReroll) : Command {
    override fun execute(state: Game, controller: GameController) {
            team.rerolls.add(reroll)
    }

    override fun undo(state: Game, controller: GameController) {
        team.rerolls.remove(reroll)
    }
}
