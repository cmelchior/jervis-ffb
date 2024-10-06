package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Removes a reroll from a team.
 */
class RemoveTeamReroll(private val team: Team, private val reroll: TeamReroll) : Command {
    override fun execute(state: Game, controller: GameController) {
        if (!team.rerolls.remove(reroll)) {
            INVALID_GAME_STATE("Could not remove reroll from ${team.name}: $reroll")
        }
    }

    override fun undo(state: Game, controller: GameController) {
        team.rerolls.add(reroll)
    }
}
