package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetKickingTeamAtHalfTime(private val kickingTeam: Team) : Command {
    private lateinit var originalKickingTeam: Team

    override fun execute(
        state: Game,
    ) {
        this.originalKickingTeam = state.kickingTeamInLastHalf
        state.kickingTeamInLastHalf = kickingTeam
        state.kickingTeam = kickingTeam
        state.receivingTeam = kickingTeam.otherTeam()
    }

    override fun undo(
        state: Game,
    ) {
        state.receivingTeam = originalKickingTeam.otherTeam()
        state.kickingTeam = originalKickingTeam
        state.kickingTeamInLastHalf = originalKickingTeam
    }
}
