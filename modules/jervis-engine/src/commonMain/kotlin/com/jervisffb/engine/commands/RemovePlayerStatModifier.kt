package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.StatModifier

class RemovePlayerStatModifier(private val player: Player, val modifier: StatModifier) : Command {
    override fun execute(state: Game, controller: GameController) {
        player.apply {
            removeStatModifier(modifier)
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            addStatModifier(modifier)
            notifyUpdate()
        }
    }
}
