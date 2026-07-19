package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.utils.INVALID_GAME_STATE

class SetTurnOver(private val status: TurnOver?) : Command {
    private var originalValue: TurnOver? = null

    override fun execute(state: Game) {
        originalValue = state.turnOver
        if (state.turnOver != null && status != null && state.turnOver != status) {
            INVALID_GAME_STATE("Attempting to override an already existing turn over: ${state.turnOver} with $status")
        }
        state.turnOver = status
    }

    override fun undo(state: Game) {
        state.turnOver = originalValue
    }
}
