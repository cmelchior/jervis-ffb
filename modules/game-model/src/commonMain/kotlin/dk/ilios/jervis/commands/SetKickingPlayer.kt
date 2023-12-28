package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetKickingPlayer(private val player: Player?) : Command {

    private var originalPlayer: Player? = null

    override fun execute(state: Game, controller: GameController) {
        this.originalPlayer = state.activePlayer
        state.kickingPlayer = player
    }

    override fun undo(state: Game, controller: GameController) {
        state.kickingPlayer = originalPlayer
    }
}
