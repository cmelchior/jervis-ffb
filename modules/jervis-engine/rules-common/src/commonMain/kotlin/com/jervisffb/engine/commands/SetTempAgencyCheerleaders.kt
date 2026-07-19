package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetTempAgencyCheerleaders(private val team: Team, private val count: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.tempAgencyCheerleaders
        team.tempAgencyCheerleaders = count
    }

    override fun undo(state: Game) {
        team.tempAgencyCheerleaders = originalValue
    }
}
