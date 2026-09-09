package com.jervisffb.engine.common.comm

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.InducementEffect

class SetInducementEffectUsed(private val inducement: InducementEffect, private val used: Boolean): Command {
    var originalUsed = inducement.used
    override fun execute(state: Game) {
        inducement.used = used
    }
    override fun undo(state: Game) {
        inducement.used = originalUsed
    }
}
