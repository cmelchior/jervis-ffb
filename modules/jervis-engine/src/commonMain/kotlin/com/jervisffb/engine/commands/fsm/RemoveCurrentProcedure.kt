package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.MutableProcedureState
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
    private lateinit var originalProcedure: MutableProcedureState

    override fun execute(state: Game) {
        originalProcedure = state.removeProcedure()
        val current: MutableProcedureState? = state.currentProcedure()
        logEntry1 = SimpleLogEntry("Procedure ${originalProcedure.name()} removed.", LogCategory.STATE_MACHINE)
        logEntry2 = if (current != null) {
            SimpleLogEntry("Current state: ${current.name()}[${current.currentNode().name()}]", LogCategory.STATE_MACHINE)
        } else {
            SimpleLogEntry("Current state: <Empty>", LogCategory.STATE_MACHINE)
        }
        state.addLog(logEntry1)
        state.addLog(logEntry2)
    }

    override fun undo(state: Game) {
        state.removeLog(logEntry2)
        state.removeLog(logEntry1)
        state.addProcedure(originalProcedure)
    }
}
