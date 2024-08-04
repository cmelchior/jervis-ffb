package dk.ilios.jervis.commands

import compositeCommandOf
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

interface Command {
    fun execute(
        state: Game,
        controller: GameController,
    )

    fun undo(
        state: Game,
        controller: GameController,
    )

    operator fun plus(other: Command): Command {
        return compositeCommandOf(this, other)
    }
}
