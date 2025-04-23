package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary

class UseApothecary(private val team: Team, private val apothecary: Apothecary) : Command {
    private var originalUsed: Boolean = false

    override fun execute(state: Game) {
        originalUsed = apothecary.used
        apothecary.used = true
    }

    override fun undo(
        state: Game,
    ) {
        apothecary.used = false
    }
}
