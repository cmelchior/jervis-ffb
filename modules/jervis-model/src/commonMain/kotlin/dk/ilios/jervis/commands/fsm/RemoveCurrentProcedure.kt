package dk.ilios.jervis.commands.fsm

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.ProcedureState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.LogEntry
import dk.ilios.jervis.reports.SimpleLogEntry

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
