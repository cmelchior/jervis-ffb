package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetTreasury(private val team: Team, private val newValue: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.treasury
        team.treasury = newValue
    }

    override fun undo(state: Game) {
        team.treasury = originalValue
    }
}
