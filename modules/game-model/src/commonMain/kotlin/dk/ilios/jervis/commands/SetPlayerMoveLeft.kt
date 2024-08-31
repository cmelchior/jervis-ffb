package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

/**
 * Set how many normal move squares the player has left. This does not include
 * Rush or actions that provide more move (I think).
 */
class SetPlayerMoveLeft(private val player: Player, val remainingMove: Int) : Command {
    private var originalMove: Int = 0

    init {
        if (remainingMove < 0) throw IllegalArgumentException("Remaining move cannot be negative")
    }

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        this.originalMove = player.movesLeft
        player.apply {
            movesLeft = remainingMove
            notifyUpdate()
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        player.apply {
            movesLeft = originalMove
            notifyUpdate()
        }
    }
}
