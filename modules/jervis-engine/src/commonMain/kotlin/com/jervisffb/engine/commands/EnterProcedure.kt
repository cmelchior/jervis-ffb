package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.SimpleLogEntry

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
