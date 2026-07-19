package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

/**
 * Sets the kicking team for the next drive (which will also set the receiving team)
 */
class SetKickingTeam(private val kickingTeam: Team) : Command {
    private lateinit var originalKickingTeam: Team
    private lateinit var originalReceivingTeam: Team

    override fun execute(state: Game) {
        originalKickingTeam = state.kickingTeam
        originalReceivingTeam = state.receivingTeam
        state.kickingTeam = kickingTeam
        state.receivingTeam = kickingTeam.otherTeam()
    }

    override fun undo(state: Game) {
        state.kickingTeam = originalKickingTeam
        state.receivingTeam = originalReceivingTeam
    }
}
