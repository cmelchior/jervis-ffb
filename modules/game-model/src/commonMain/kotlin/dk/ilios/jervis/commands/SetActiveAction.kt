package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.PlayerAction

class SetActiveAction(private val action: PlayerAction?) : Command {
    private var originalAction: PlayerAction? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalAction = state.activePlayerAction
        state.activePlayerAction = action
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.activePlayerAction = originalAction
    }
}
