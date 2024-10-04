package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

/**
 * Set how many rushes a player can perform during the current action.
 * As per page 44 in the rulebook, rushes are available pr. action and not
 * pr. turn, so we need to set this separately from [SetPlayerTemporaryStats].
 */
class SetPlayerRushesLeft(private val player: Player, val remainingRushes: Int) : Command {
    private var originalRushes: Int = 0

    override fun execute(state: Game, controller: GameController) {
        this.originalRushes = player.rushesLeft
        player.apply {
            rushesLeft = remainingRushes
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            rushesLeft = originalRushes
            notifyUpdate()
        }
    }
}
