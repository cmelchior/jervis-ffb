package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry

class EnterProcedure(private val procedure: Procedure) : Command {
    private val enterProcedureEntry =
        SimpleLogEntry(message = "Load procedure: ${procedure.name()}[${procedure.initialNode.name()}]", LogCategory.STATE_MACHINE)

    override fun execute(state: Game, controller: GameController) {
        controller.addLog(enterProcedureEntry)
        controller.addProcedure(procedure)
    }

    override fun undo(state: Game, controller: GameController) {
        controller.removeProcedure()
        controller.removeLog(enterProcedureEntry)
    }
}
