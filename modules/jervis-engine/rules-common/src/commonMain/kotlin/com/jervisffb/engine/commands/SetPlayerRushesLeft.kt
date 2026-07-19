package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

/**
 * Set how many rushes a player can perform during the current action.
 * As per page 44 in the rulebook, rushes are available pr. action and not
 * pr. turn, so we need to set this separately from [SetPlayerTemporaryStats].
 */
class SetPlayerRushesLeft(private val player: Player, val remainingRushes: Int) : Command {
    private var originalRushes: Int = 0

    override fun execute(state: Game) {
        this.originalRushes = player.rushesLeft
        player.apply {
            rushesLeft = remainingRushes
        }
    }

    override fun undo(state: Game) {
        player.apply {
            rushesLeft = originalRushes
        }
    }
}
