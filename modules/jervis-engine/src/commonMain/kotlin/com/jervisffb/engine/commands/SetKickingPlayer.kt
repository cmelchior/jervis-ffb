package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetKickingPlayer(private val player: Player?) : Command {
    private var originalPlayer: Player? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        this.originalPlayer = state.activePlayer
        state.kickingPlayer = player
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.kickingPlayer = originalPlayer
    }
}
