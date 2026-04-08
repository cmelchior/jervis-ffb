package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Normally we do not allow overriding turn-over states, but if we are aware
 * that is happening. This command can do it to actively allow it.
 *
 * This command cannot be used to change the state from `null`. In that case,
 * [SetTurnOver] must be used.
 */
class UpdateTurnOver(private val status: TurnOver) : Command {
    private lateinit var originalValue: TurnOver

    override fun execute(state: Game) {
        if (state.turnOver == null) {
            INVALID_GAME_STATE("Cannot override null turn-over state with: $status")
        }
        originalValue = state.turnOver!!
        state.turnOver = status
    }

    override fun undo(state: Game) {
        state.turnOver = originalValue
    }
}
