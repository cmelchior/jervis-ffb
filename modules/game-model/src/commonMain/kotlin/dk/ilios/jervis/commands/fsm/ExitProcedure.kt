package dk.ilios.jervis.commands.fsm

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Exit the current procedure.
 *
 * Before this happens, [Procedure.onExitProcedure] is called.
 */
class ExitProcedure : Command {
    private lateinit var originalNode: Node

    override fun execute(state: Game, controller: GameController) {
        originalNode = controller.currentProcedure()?.currentNode() ?: INVALID_GAME_STATE("No procedure is running.")
        val currentProcedure = controller.currentProcedure()!!
        currentProcedure.setCurrentNode(currentProcedure.procedure.exitNode)
    }

    override fun undo(state: Game, controller: GameController) {
        // Remove the `exitNode`
        controller.currentProcedure()!!.setCurrentNode(originalNode)
    }
}
