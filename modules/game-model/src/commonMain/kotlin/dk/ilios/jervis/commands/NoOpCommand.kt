package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

data object NoOpCommand : Command {
    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        // Do nothing
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        // Do nothing
    }
}
