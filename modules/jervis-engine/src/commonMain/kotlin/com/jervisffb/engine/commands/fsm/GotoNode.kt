package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.utils.INVALID_GAME_STATE

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
