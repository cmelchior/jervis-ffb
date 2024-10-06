package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.Game

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
