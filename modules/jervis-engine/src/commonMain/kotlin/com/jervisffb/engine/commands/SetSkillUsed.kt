package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2020.skills.Skill

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
