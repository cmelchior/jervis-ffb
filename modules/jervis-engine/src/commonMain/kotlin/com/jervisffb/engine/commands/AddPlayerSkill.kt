package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.rules.bb2020.skills.Skill

class AddPlayerSkill(private val player: Player, val skill: Skill) : Command {
    override fun execute(state: Game) {
        player.addSkill(skill)
        player.notifyUpdate()
    }

    override fun undo(state: Game) {
        player.removeSkill(skill)
        player.notifyUpdate()
    }
}
