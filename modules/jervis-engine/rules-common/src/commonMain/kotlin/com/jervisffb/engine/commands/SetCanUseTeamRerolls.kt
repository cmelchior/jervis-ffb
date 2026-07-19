package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game

class SetCanUseTeamRerolls(private val canUseRerolls: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game) {
        originalValue = state.canUseTeamRerolls
        state.canUseTeamRerolls = canUseRerolls
    }

    override fun undo(state: Game) {
        state.canUseTeamRerolls = originalValue
    }
}
