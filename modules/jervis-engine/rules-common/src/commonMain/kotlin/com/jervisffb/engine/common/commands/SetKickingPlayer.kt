package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetKickingPlayer(private val player: Player?) : Command {
    private var originalPlayer: Player? = null

    override fun execute(
        state: Game,
    ) {
        this.originalPlayer = state.activePlayer
        state.kickingPlayer = player
    }

    override fun undo(
        state: Game,
    ) {
        state.kickingPlayer = originalPlayer
    }
}
