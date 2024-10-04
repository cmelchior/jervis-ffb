package dk.ilios.jervis.commands

import compositeCommandOf
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game

interface Command {
    // TODO The only reason to pass in GameController is for the internal
    //  commands (like ExitProcedure) to manipulate the stack.
    //  would be nice to remove it.
    //  We could move ProcedureStack into `Game`, but is that considered part of the game "state"
    //  How many use cases is there where we want to attach a different procedure stack to a
    //  given state? Probably none? And even if there is, it could be done inside the game state.
    //  Only argument is probably only philosophical, i.e "Rules" and "State" being different
    fun execute(state: Game, controller: GameController)
    fun undo(state: Game, controller: GameController)
    operator fun plus(other: Command): Command {
        return compositeCommandOf(this, other)
    }
}
