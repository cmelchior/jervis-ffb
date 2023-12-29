package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ProcedureState
import dk.ilios.jervis.reports.LogEntry
import dk.ilios.jervis.model.Game

class ExitProcedure: Command {
    private lateinit var currentNode: Node
    private var entry2: LogEntry? = null
    private lateinit var entry1: LogEntry
    private lateinit var currentProcedure: ProcedureState

    override fun execute(state: Game, controller: GameController) {
        currentNode = controller.currentProcedure()!!.currentNode()
        controller.currentProcedure()!!.gotoExit()
//
//        val removed: ProcedureState = controller.removeProcedure()
//        currentProcedure = removed.copy()
//        val current: ProcedureState? = controller.currentProcedure()
//        entry1 = SimpleLogEntry("Procedure ${removed.name()} removed.")
//        if (current != null) {
//            entry2 = SimpleLogEntry("Current state: ${current.name()}[${current.currentNode().name()}]")
//        } else {
//            entry2 = SimpleLogEntry("Current state: <Empty>")
//        }
//        controller.addLog(entry1)
//        entry2?.let { controller.addLog(it) }
    }

    override fun undo(state: Game, controller: GameController) {
//        entry2?.let { controller.removeLog(it) }
//        controller.removeLog(entry1)
        controller.currentProcedure()!!.removeLast()
//        controller.addProcedure(currentProcedure)
    }
}