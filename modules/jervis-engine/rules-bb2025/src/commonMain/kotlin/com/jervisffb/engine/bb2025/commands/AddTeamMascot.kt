package com.jervisffb.engine.bb2025.commands

import com.jervisffb.engine.bb2025.inducements.StandardTeamMascot
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class AddTeamMascot(private val team: Team) : Command {
    private val mascot = StandardTeamMascot(team.id)

    override fun execute(state: Game) {
        team.mascots.add(mascot)
        team.rerolls.add(mascot.reroll)
    }
    override fun undo(state: Game) {
        team.rerolls.remove(mascot.reroll)
        team.mascots.remove(mascot)
    }
}
