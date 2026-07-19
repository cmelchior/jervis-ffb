package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

/**
 * Set the Active and Inactive teams.
 * Note, this concept is only relevant during team turns, not outside them.
 */
class SetActiveTeam(private val activeTeam: Team?) : Command {
    private var originalTeam: Team? = null

    override fun execute(state: Game) {
        originalTeam = state.activeTeam
        state.activeTeam = activeTeam
        state.inactiveTeam = activeTeam?.otherTeam()
    }

    override fun undo(state: Game) {
        state.inactiveTeam = originalTeam?.otherTeam()
        state.activeTeam = originalTeam
    }
}
