package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll

/**
 * Add a new reroll to the team.
 */
class AddTeamReroll(private val team: Team, private val reroll: TeamReroll) : Command {
    override fun execute(state: Game) {
        team.rerolls.add(reroll)
    }

    override fun undo(state: Game) {
        team.rerolls.remove(reroll)
    }
}
