package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver

class SetTurnOver(private val status: TurnOver?) : Command {
    private var originalValue: TurnOver? = null

    override fun execute(state: Game) {
        originalValue = state.turnOver
        state.turnOver = status
    }

    override fun undo(state: Game) {
        state.turnOver = originalValue
    }
}
