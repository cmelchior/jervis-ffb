package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.common.skills.RerollSource

class SetSkillRerollUsed(private val source: RerollSource, private val used: Boolean) : Command {
    private var original: Boolean = false

    override fun execute(state: Game) {
        original = source.rerollUsed
        source.rerollUsed = used
    }

    override fun undo(state: Game) {
        source.rerollUsed = original
    }
}
