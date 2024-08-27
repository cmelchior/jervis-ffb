package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

/**
 * Set all the player's stats that can change during a turn. This includes:
 *
 * - Moves left
 * - Rushes left
 * - Temporary skills
 * - Temporary stat modifiers
 */
class SetPlayerStats(
    private val player: Player,
    private val movesLeft: Int,
    private val rushesLeft: Int
): Command {
    private val originalMovesLeft: Int = player.movesLeft
    private val originalRushesLeft: Int = player.rushesLeft

    override fun execute(state: Game, controller: GameController) {
        player.apply {
            this@apply.movesLeft = this@SetPlayerStats.movesLeft
            this@apply.rushesLeft = this@SetPlayerStats.rushesLeft
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            movesLeft = originalMovesLeft
            rushesLeft = originalRushesLeft
            notifyUpdate()
        }
    }
}
