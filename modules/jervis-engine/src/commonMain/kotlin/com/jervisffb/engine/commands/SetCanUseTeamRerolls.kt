package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game

class SetCanUseTeamRerolls(private val canUseRerolls: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalValue = state.canUseTeamRerolls
        state.canUseTeamRerolls = canUseRerolls
    }

    override fun undo(state: Game, controller: GameController) {
        state.canUseTeamRerolls = originalValue
    }
}
