package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team

class SetBlitzersBestKegs(private val team: Team, private val count: Int) : Command {
    private var originalValue: Int = 0

    override fun execute(state: Game) {
        originalValue = team.blitzersBestKegs
        team.blitzersBestKegs = count
    }

    override fun undo(state: Game) {
        team.blitzersBestKegs = originalValue
    }
}
