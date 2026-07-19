package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.Bribe

class SetBribeUsed(private val bribe: Bribe, private val used: Boolean) : Command {
    var originalUsed: Boolean = false
    override fun execute(state: Game) {
        originalUsed = bribe.used
        bribe.used = used
    }

    override fun undo(state: Game) {
        bribe.used = originalUsed
    }
}
