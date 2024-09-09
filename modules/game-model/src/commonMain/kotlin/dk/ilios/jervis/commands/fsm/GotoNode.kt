package dk.ilios.jervis.commands.fsm

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.LogEntry
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.utils.INVALID_GAME_STATE

class GotoNode(private val nextNode: Node) : Command {
    private lateinit var logEntry: LogEntry
    private lateinit var originalNode: Node
    private lateinit var originalParentState: ParentNode.State

    override fun execute(state: Game, controller: GameController) {
        val currentProcedure = controller.currentProcedure() ?: INVALID_GAME_STATE("No procedure is running.")
        logEntry = SimpleLogEntry("Transition to: ${currentProcedure.name()}[${nextNode.name()}]", LogCategory.STATE_MACHINE)
        controller.addLog(logEntry)
        originalNode = currentProcedure.currentNode()
        if (originalNode is ParentNode) {
            originalParentState = currentProcedure.getParentNodeState()
        }
        controller.setCurrentNode(nextNode)
        if (nextNode is ParentNode) {
            currentProcedure.setParentNodeState(ParentNode.State.ENTERING)
        }
    }

    override fun undo(state: Game, controller: GameController) {
        val currentProcedure = controller.currentProcedure()!!
        if (nextNode is ParentNode) {
            currentProcedure.setParentNodeState(null)
        }
        controller.setCurrentNode(originalNode)
        if (originalNode is ParentNode) {
            currentProcedure.setParentNodeState(originalParentState)
        }
        controller.removeLog(logEntry)
    }
}
