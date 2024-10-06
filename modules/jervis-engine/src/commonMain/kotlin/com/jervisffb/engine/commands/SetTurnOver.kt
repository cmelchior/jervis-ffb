package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver

class SetTurnOver(private val status: TurnOver?) : Command {
    private var originalValue: TurnOver? = null

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.turnOver
        state.turnOver = status
    }

    override fun undo(state: Game, controller: GameController) {
        state.turnOver = originalValue
    }
}
