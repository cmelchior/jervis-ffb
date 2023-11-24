package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.fsm.Node
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.fsm.Procedure
import dk.ilios.bowlbot.logs.LogCategory
import dk.ilios.bowlbot.logs.LogEntry
import dk.ilios.bowlbot.logs.SimpleLogEntry
import dk.ilios.bowlbot.model.Game

class GotoNode(private val nextNode: Node): Command {
    private lateinit var logEntry1: LogEntry
    private var logEntry2: LogEntry? = null
    override fun execute(state: Game, controller: GameController) {
        logEntry1 = SimpleLogEntry("Transition to: ${controller.currentProcedure()!!.name()}[${nextNode.name()}]")
        controller.addLog(logEntry1)
        controller.addNode(nextNode)
        if (nextNode is ParentNode) {
            controller.currentProcedure()!!.addParentNodeState(ParentNode.State.ENTERING)
        }
        if (nextNode is Procedure) {
            TODO()
//            logEntry2 = SimpleLogEntry(LogCategory.STATE_MACHINE, "Load procedure: ${nextNode.name()}[${nextNode.initialNode.name()}]")
//            controller.addLog(logEntry2!!)
//            controller.addProcedure(nextNode)
        }
    }

    override fun undo(state: Game, controller: GameController) {
        if (nextNode is Procedure) {
            TODO()
//            controller.removeProcedure()
//            controller.removeLog(logEntry2!!)
        }
        if (controller.currentProcedure()!!.currentNode() is ParentNode) {
            controller.currentProcedure()!!.removeParentNodeState(ParentNode.State.ENTERING)
        }
        controller.removeNode()
        controller.removeLog(logEntry1)
    }
}