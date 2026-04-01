package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.modifiers.TeamStatusEffect

class RemoveTeamStatusEffect(private val team: Team, val effect: TeamStatusEffect) : Command {
    override fun execute(state: Game) {
        team.removeStatusEffect(effect)
    }

    override fun undo(state: Game) {
        team.addStatusEffect(effect)
    }
}
