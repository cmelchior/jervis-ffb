package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetActivePlayer(private val player: Player?) : Command {

    private var originalPlayer: Player? = null

    override fun execute(state: Game, controller: GameController) {
        originalPlayer = state.activePlayer
        state.activePlayer = player
        originalPlayer?.notifyUpdate()
        state.activePlayer?.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        val old = state.activePlayer
        state.activePlayer = null
        old?.notifyUpdate()
        state.activePlayer = originalPlayer
        state.activePlayer?.notifyUpdate()
    }
}
