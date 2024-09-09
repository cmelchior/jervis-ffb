package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.LogEntry
import dk.ilios.jervis.reports.SimpleLogEntry

class GotoNode(private val nextNode: Node) : Command {
    private lateinit var logEntry1: LogEntry

    override fun execute(state: Game, controller: GameController) {
        logEntry1 = SimpleLogEntry("Transition to: ${controller.currentProcedure()!!.name()}[${nextNode.name()}]", LogCategory.STATE_MACHINE)
        controller.addLog(logEntry1)
        controller.addNode(nextNode)
        if (nextNode is ParentNode) {
            controller.currentProcedure()!!.addParentNodeState(ParentNode.State.ENTERING)
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (controller.currentProcedure()!!.currentNode() is ParentNode) {
            controller.currentProcedure()!!.removeParentNodeState(ParentNode.State.ENTERING)
        }
        controller.removeNode()
        controller.removeLog(logEntry1)
    }
}
