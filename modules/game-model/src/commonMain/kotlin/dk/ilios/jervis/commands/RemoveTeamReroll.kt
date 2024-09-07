package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.skills.TeamReroll
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Removes a reroll from a team.
 */
class RemoveTeamReroll(private val team: Team, private val reroll: TeamReroll) : Command {
    override fun execute(state: Game, controller: GameController) {
        if (!team.rerolls.remove(reroll)) {
            INVALID_GAME_STATE("Could not remove reroll from ${team.name}: $reroll")
        }
    }

    override fun undo(state: Game, controller: GameController) {
        team.rerolls.add(reroll)
    }
}
