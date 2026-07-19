package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetPartTimeAssistantCoaches(private val team: Team, private val count: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.partTimeAssistantCoaches
        team.partTimeAssistantCoaches = count
    }

    override fun undo(state: Game) {
        team.partTimeAssistantCoaches = originalValue
    }
}
