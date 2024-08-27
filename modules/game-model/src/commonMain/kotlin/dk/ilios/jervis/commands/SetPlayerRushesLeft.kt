package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

/**
 * Set how many normal move squares the player has left. This does not include
 * Rush or actions that provide more move (I think).
 */
class SetPlayerRushesLeft(private val player: Player, val remainingRushes: Int) : Command {
    private var originalRushes: Int = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        this.originalRushes = player.rushesLeft
        player.apply {
            rushesLeft = remainingRushes
            notifyUpdate()
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        player.apply {
            rushesLeft = originalRushes
            notifyUpdate()
        }
    }
}
