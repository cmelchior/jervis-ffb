package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetActivePlayer(private val player: Player?) : Command {
    private var originalPlayer: Player? = null

    override fun execute(
        state: Game,
    ) {
        originalPlayer = state.activePlayer
        state.activePlayer = player
        originalPlayer?.notifyUpdate()
        state.activePlayer?.notifyUpdate()
    }

    override fun undo(
        state: Game,
    ) {
        val old = state.activePlayer
        state.activePlayer = null
        old?.notifyUpdate()
        state.activePlayer = originalPlayer
        state.activePlayer?.notifyUpdate()
    }
}
