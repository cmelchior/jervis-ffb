package dk.ilios.bowlbot.commands

import dk.ilios.bowlbot.controller.GameController
import dk.ilios.bowlbot.fsm.ParentNode
import dk.ilios.bowlbot.model.Game

// Sets the state of the current parent node.
class ChangeParentNodeState(private val nextState: ParentNode.State) : Command {
    override fun execute(state: Game, controller: GameController) {
        controller.stack.firstOrNull()!!.addParentNodeState(nextState)
    }

    override fun undo(state: Game, controller: GameController) {
        controller.stack.firstOrNull()!!.removeParentNodeState(nextState)
    }
}
