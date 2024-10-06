package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.fsm.ProcedureState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.SimpleLogEntry

/**
 * For internal use only.
 */
class RemoveCurrentProcedure : Command {
    private lateinit var logEntry1: LogEntry
    private lateinit var logEntry2: LogEntry
    private lateinit var originalProcedure: ProcedureState

    override fun execute(state: Game, controller: GameController) {
        originalProcedure = controller.removeProcedure()
        val current: ProcedureState? = controller.currentProcedure()
        logEntry1 = SimpleLogEntry("Procedure ${originalProcedure.name()} removed.", LogCategory.STATE_MACHINE)
        logEntry2 = if (current != null) {
            SimpleLogEntry("Current state: ${current.name()}[${current.currentNode().name()}]", LogCategory.STATE_MACHINE)
        } else {
            SimpleLogEntry("Current state: <Empty>", LogCategory.STATE_MACHINE)
        }
        controller.addLog(logEntry1)
        controller.addLog(logEntry2)
    }

    override fun undo(state: Game, controller: GameController) {
        controller.removeLog(logEntry2)
        controller.removeLog(logEntry1)
        controller.addProcedure(originalProcedure)
    }
}
