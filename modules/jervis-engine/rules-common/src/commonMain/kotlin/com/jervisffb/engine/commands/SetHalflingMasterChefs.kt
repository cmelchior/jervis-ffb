package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetHalflingMasterChefs(private val team: Team, private val count: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.halflingMasterChefs
        team.halflingMasterChefs = count
    }

    override fun undo(state: Game) {
        team.halflingMasterChefs = originalValue
    }
}
