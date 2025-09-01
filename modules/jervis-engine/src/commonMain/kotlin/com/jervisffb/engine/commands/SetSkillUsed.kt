package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.skills.Skill

/**
 * Mark a skill as have being used or not.
 */
class SetSkillUsed(private val player: Player, private val skill: Skill, val used: Boolean) : Command {
    private var originalUsed: Boolean = false

    override fun execute(state: Game) {
        this.originalUsed = skill.used
        skill.used = this@SetSkillUsed.used
    }

    override fun undo(state: Game) {
        skill.used = originalUsed
    }
}
