package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.setContext

/**
 * Save a new [ProcedureContext]
 */
class SetContext(private val context: ProcedureContext) : Command {
    var originalValue: ProcedureContext? = null

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.contexts.getContext(context::class)
        state.setContext(context)
    }

    override fun undo(state: Game, controller: GameController) {
        if (originalValue == null) {
            state.contexts.remove(context::class)
        } else {
            state.setContext(originalValue!!)
        }
    }
}
