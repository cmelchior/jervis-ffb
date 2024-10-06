package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class AddNigglingInjuries(val player: Player, val change: Int): Command {
    var originalValue: Int = 0
    override fun execute(state: Game, controller: GameController) {
        originalValue = player.nigglingInjuries
        player.apply {
            nigglingInjuries += change
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            nigglingInjuries = originalValue
            notifyUpdate()
        }
    }
}
