package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetFanFactor(private val team: Team, private val fanFactor: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(
        state: Game,
    ) {
        originalValue = team.fanFactor
        team.apply {
            fanFactor = this@SetFanFactor.fanFactor
        }
    }

    override fun undo(
        state: Game,
    ) {
        team.apply {
            fanFactor = originalValue
        }
    }
}
