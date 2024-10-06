package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.skills.RerollSource

class SetSkillRerollUsed(private val source: RerollSource) : Command {
    private var original: Boolean = false

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        original = source.rerollUsed
        source.rerollUsed = true
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        source.rerollUsed = original
    }
}
