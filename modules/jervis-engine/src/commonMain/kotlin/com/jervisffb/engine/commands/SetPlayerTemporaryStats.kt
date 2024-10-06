package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

/**
 * Set all the player's stats that can change during a turn. This includes:
 *
 * - Moves left
 * - Temporary skills
 * - Temporary stat modifiers
 */
class SetPlayerTemporaryStats(private val player: Player, private val movesLeft: Int): Command {
    private val originalMovesLeft: Int = player.movesLeft

    override fun execute(state: Game, controller: GameController) {
        player.apply {
            this@apply.movesLeft = this@SetPlayerTemporaryStats.movesLeft
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            movesLeft = originalMovesLeft
            notifyUpdate()
        }
    }
}
