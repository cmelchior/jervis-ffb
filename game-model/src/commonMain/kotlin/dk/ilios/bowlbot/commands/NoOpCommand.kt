package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game

data object NoOpCommand: Command {
    override fun execute(state: Game, controller: GameController) {
        /* Do nothing */
    }
    override fun undo(state: Game, controller: GameController) {
        /* Do nothing */
    }
}