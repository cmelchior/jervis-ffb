package com.jervisffb.engine.commands.fsm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * For internal use only.
 *
 * Sets the state of the current parent node.
 */
class ChangeParentNodeState(private val nextState: ParentNode.State) : Command {
    private var originalParentState: ParentNode.State? = null

    override fun execute(state: Game, controller: GameController) {
        val procedureState = controller.stack.peepOrNull() ?: INVALID_GAME_STATE("No procedure is running.")
        originalParentState = procedureState.getParentNodeState()
        procedureState.setParentNodeState(nextState)
    }

    override fun undo(state: Game, controller: GameController) {
        controller.stack.peepOrNull()?.setParentNodeState(originalParentState) ?: INVALID_GAME_STATE("No procedure is running.")
    }
}
