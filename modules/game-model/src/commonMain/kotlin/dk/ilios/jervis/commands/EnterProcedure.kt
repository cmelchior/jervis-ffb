package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.model.Game

class EnterProcedure(private val procedure: Procedure): Command {
    private val enterProcedureEntry = SimpleLogEntry(message = "Load procedure: ${procedure.name()}[${procedure.initialNode.name()}]")
    override fun execute(state: Game, controller: GameController) {
        controller.addLog(enterProcedureEntry)
        controller.addProcedure(procedure)
    }
    override fun undo(state: Game, controller: GameController) {
        controller.removeProcedure()
        controller.removeLog(enterProcedureEntry)
    }
}