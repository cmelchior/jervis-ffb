package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player

/**
 * Set how many normal move squares the player has left. This does not include
 * Rush or actions that provide more move (I think).
 */
class SetPlayerMoveLeft(private val player: Player, val move: Int) : Command {
    private var originalMove: Int = 0
    override fun execute(state: Game, controller: GameController) {
        this.originalMove = player.moveLeft
        player.moveLeft = move
    }

    override fun undo(state: Game, controller: GameController) {
        player.moveLeft = originalMove
    }
}
