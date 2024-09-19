package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.Game

class SetMoveStepTarget(private val from: FieldCoordinate, private val to: FieldCoordinate) : Command {
    private var originalTarget: Pair<FieldCoordinate, FieldCoordinate>? = null

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalTarget = state.moveStepTarget
        state.moveStepTarget = Pair(from, to)
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        state.moveStepTarget = originalTarget
    }
}
