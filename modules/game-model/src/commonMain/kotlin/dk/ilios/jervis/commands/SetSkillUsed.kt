package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.skills.Skill

/**
 * Mark a skill as have being used or not.
 */
class SetSkillUsed(private val player: Player, private val skill: Skill, val used: Boolean) : Command {
    private var originalUsed: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        this.originalUsed = skill.used
        skill.used = this@SetSkillUsed.used
        player.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        skill.used = originalUsed
        player.notifyUpdate()
    }
}
