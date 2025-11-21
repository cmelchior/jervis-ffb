package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect

class AddPlayerStatusEffect(private val player: Player, val effect: PlayerStatusEffect) : Command {
    override fun execute(state: Game) {
        player.addStatusEffect(effect)
    }

    override fun undo(state: Game) {
        player.removeStatusEffect(effect)
    }
}
