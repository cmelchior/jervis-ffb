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

    lateinit var procedure: Procedure
    private lateinit var originalNode: Node

    override fun execute(state: Game) {
        val procedureState = state.currentProcedure() ?: INVALID_GAME_STATE("No procedure is running.")
        procedure = procedureState.procedure
        originalNode = procedureState.currentNode()
        val currentProcedure = state.currentProcedure()!!
        currentProcedure.setCurrentNode(currentProcedure.procedure.exitNode)
    }

    override fun undo(state: Game) {
        // Remove the `exitNode`
        state.currentProcedure()!!.setCurrentNode(originalNode)
    }
}
