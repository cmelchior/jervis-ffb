package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetMissNextGame(
    private val player: Player,
    private val missNextGame: Boolean,
) : Command {
    var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        this.originalValue = player.missNextGame
        player.apply {
            missNextGame = this@SetMissNextGame.missNextGame
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            missNextGame = originalValue
            notifyUpdate()
        }
    }
}
