package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.skills.Skill

class AddPlayerSkill(private val player: Player, val skill: Skill) : Command {
    override fun execute(state: Game, controller: GameController) {
        player.addSkill(skill)
        player.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        player.removeSkill(skill)
        player.notifyUpdate()
    }
}
