package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.SimpleLogEntry
import dk.ilios.bowlbot.model.Game

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