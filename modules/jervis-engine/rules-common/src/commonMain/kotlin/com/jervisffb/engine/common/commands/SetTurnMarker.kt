package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetTurnMarker(private val currentTeam: Team, private val nextTurn: Int) : Command {
    private var originalTurn: Int = 0

    override fun execute(state: Game) {
        originalTurn = currentTeam.turnMarker
        currentTeam.turnMarker = nextTurn
    }

    override fun undo(state: Game) {
        currentTeam.turnMarker = originalTurn
    }
}
