package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.common.skills.Skill

class RemovePlayerSkill(private val player: Player, val skill: Skill) : Command {
    override fun execute(state: Game) {
        player.removeSkill(skill)
    }

    override fun undo(state: Game) {
        player.addSkill(skill)
    }
}
