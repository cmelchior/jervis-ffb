package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.model.Game

// Sets the state of the current parent node.
/**
 * For internal use only.
 *
 * This command alters the state of the Procedure r
 */
class ChangeParentNodeState(private val nextState: ParentNode.State) : Command {
    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        controller.stack.firstOrNull()!!.addParentNodeState(nextState)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        controller.stack.firstOrNull()!!.removeParentNodeState(nextState)
    }
}
