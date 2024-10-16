package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.TemporaryEffect

class AddPlayerTemporaryEffect(private val player: Player, val effect: TemporaryEffect) : Command {
    override fun execute(state: Game) {
        player.temporaryEffects.add(effect)
        player.notifyUpdate()
    }

    override fun undo(state: Game) {
        player.temporaryEffects.remove(effect)
        player.notifyUpdate()
    }
}
