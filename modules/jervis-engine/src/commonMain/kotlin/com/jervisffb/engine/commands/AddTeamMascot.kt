package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.TeamMascot

class AddTeamMascot(private val team: Team) : Command {
    private val mascot = TeamMascot(team.id)

    override fun execute(state: Game) {
        team.mascots.add(mascot)
        team.rerolls.add(mascot.reroll)
    }
    override fun undo(state: Game) {
        team.rerolls.remove(mascot.reroll)
        team.mascots.remove(mascot)
    }
}
