package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

/**
 * Set all the player's stats that can change during a turn. This includes:
 *
 * - Moves left
 * - Temporary skills
 * - Temporary stat modifiers
 */
class SetPlayerStats(
    private val player: Player,
    private val movesLeft: Int,
): Command {
    private val originalMovesLeft: Int = player.movesLeft

    override fun execute(state: Game, controller: GameController) {
        player.apply {
            this@apply.movesLeft = this@SetPlayerStats.movesLeft
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
