package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate

class SetMoveStepTarget(private val from: FieldCoordinate, private val to: FieldCoordinate) : Command {
    private var originalTarget: Pair<FieldCoordinate, FieldCoordinate>? = null

    override fun execute(
        state: Game,
    ) {
        originalTarget = state.moveStepTarget
        state.moveStepTarget = Pair(from, to)
    }

    override fun undo(
        state: Game,
    ) {
        state.moveStepTarget = originalTarget
    }
}
