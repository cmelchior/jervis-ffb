package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.GameEngineController.Companion.LOG
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry
import com.jervisffb.engine.reports.SimpleLogEntry
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Command for moving around in the state machine. Use this command to
 * move to another [Node] inside the current [Procedure]
 */
class GotoNode(private val nextNode: Node) : Command {
    private lateinit var logEntry: LogEntry
    private lateinit var originalNode: Node
    private lateinit var originalParentState: ParentNode.State

    override fun execute(state: Game) {
        val currentProcedure = state.currentProcedure() ?: INVALID_GAME_STATE("No procedure is running.")
        LOG.v { "[Stack] Goto node: ${currentProcedure.name()}[${nextNode.name()}]" }
        logEntry = SimpleLogEntry("Transition to: ${currentProcedure.name()}[${nextNode.name()}]", LogCategory.STATE_MACHINE)
        state.addLog(logEntry)
        originalNode = currentProcedure.currentNode()
        if (originalNode is ParentNode) {
            originalParentState = currentProcedure.getParentNodeState()
        }
        state.setCurrentNode(nextNode)
        if (nextNode is ParentNode) {
            currentProcedure.setParentNodeState(ParentNode.State.CHECK_SKIP)
        }
    }

    override fun undo(state: Game) {
        val currentProcedure = state.currentProcedure()!!
        if (nextNode is ParentNode) {
            currentProcedure.setParentNodeState(null)
        }
        state.setCurrentNode(originalNode)
        if (originalNode is ParentNode) {
            currentProcedure.setParentNodeState(originalParentState)
        }
        state.removeLog(logEntry)
    }
}
