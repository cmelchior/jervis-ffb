package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.inducements.Bribe
import dk.ilios.jervis.rules.skills.Skill

class AddBribe(private val team: Team, private val bribe: Bribe) : Command {
    override fun execute(state: Game, controller: GameController) {
        team.bribes.add(bribe)
    }

    override fun undo(state: Game, controller: GameController) {
        team.bribes.remove(bribe)
    }
}
