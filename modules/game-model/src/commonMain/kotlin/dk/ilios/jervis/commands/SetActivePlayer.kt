package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetActivePlayer(private val player: Player?) : Command {

    private var originalPlayer: Player? = null

    override fun execute(state: Game, controller: GameController) {
        this.originalPlayer = state.activePlayer
        state.activePlayer = player
    }

    override fun undo(state: Game, controller: GameController) {
        state.activePlayer = originalPlayer
    }
}
