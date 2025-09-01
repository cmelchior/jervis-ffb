package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.common.skills.RerollSource

class SetTeamRerollUsed(private val source: RerollSource) : Command {
    private var original: Boolean = false

    override fun execute(state: Game,) {
        original = source.rerollUsed
        source.rerollUsed = true
    }

    override fun undo(state: Game) {
        source.rerollUsed = original
    }
}
