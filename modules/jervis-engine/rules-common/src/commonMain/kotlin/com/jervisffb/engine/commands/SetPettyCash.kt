package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetPettyCash(private val team: Team, private val newValue: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.pettyCash
        team.pettyCash = newValue
    }

    override fun undo(state: Game) {
        team.pettyCash = originalValue
    }
}
