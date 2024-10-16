package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Exit the current procedure.
 *
 * Before this happens, [Procedure.onExitProcedure] is called.
 */
class ExitProcedure : Command {
    private lateinit var originalNode: Node

    override fun execute(state: Game) {
        originalNode = state.currentProcedure()?.currentNode() ?: INVALID_GAME_STATE("No procedure is running.")
        val currentProcedure = state.currentProcedure()!!
        currentProcedure.setCurrentNode(currentProcedure.procedure.exitNode)
    }

    override fun undo(state: Game) {
        // Remove the `exitNode`
        state.currentProcedure()!!.setCurrentNode(originalNode)
    }
}
