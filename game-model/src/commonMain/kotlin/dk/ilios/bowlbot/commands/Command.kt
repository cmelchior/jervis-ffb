package dk.ilios.bowlbot.commands

import compositeCommandOf
import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.model.Game

interface Command {
    fun execute(state: Game, controller: GameController)
    fun undo(state: Game, controller: GameController)
    operator fun plus(other: Command): Command {
        return compositeCommandOf(this, other)
    }
}
