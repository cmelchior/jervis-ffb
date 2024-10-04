package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.rules.skills.SpecialActionProvider
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Mark a skill as have being used or not.
 */
class SetSpecialActionSkillUsed(private val player: Player, private val skill: Skill, val used: Boolean) : Command {
    private var originalUsed: Boolean = false

    init {
        if (skill !is SpecialActionProvider) INVALID_GAME_STATE("SpecialActionProvider is required: $skill")
    }

    override fun execute(state: Game, controller: GameController) {
        this.originalUsed = (skill as SpecialActionProvider).isSpecialActionUsed
        skill.isSpecialActionUsed = this@SetSpecialActionSkillUsed.used
        player.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        (skill as SpecialActionProvider).isSpecialActionUsed = originalUsed
        player.notifyUpdate()
    }
}
