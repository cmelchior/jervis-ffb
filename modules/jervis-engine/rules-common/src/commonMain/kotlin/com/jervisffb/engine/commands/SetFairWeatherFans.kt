package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetFairWeatherFans(private val team: Team, private val fans: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.fairWeatherFans
        team.fairWeatherFans = fans
    }

    override fun undo(state: Game) {
        team.fairWeatherFans = originalValue
    }
}
