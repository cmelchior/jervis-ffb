package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetFanFactor(private val team: Team, private val fanFactor: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        originalValue = team.fanFactor
        team.apply {
            fanFactor = this@SetFanFactor.fanFactor
            notifyUpdate()
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        team.apply {
            fanFactor = originalValue
            notifyUpdate()
        }
    }
}
