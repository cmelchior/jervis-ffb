package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate

class SetMoveStepTarget(private val from: PitchCoordinate, private val to: PitchCoordinate) : Command {
    private var originalTarget: Pair<PitchCoordinate, PitchCoordinate>? = null

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
