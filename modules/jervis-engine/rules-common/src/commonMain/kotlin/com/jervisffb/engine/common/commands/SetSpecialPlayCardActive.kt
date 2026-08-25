package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.card.SpecialPlayCard

class SetSpecialPlayCardActive(private val card: SpecialPlayCard, val active: Boolean) : Command {
    private var original: Boolean = false

    override fun execute(state: Game) {
        original = card.isActive
        card.isActive
        // TODO Notify something?
    }

    override fun undo(state: Game) {
        card.isActive = original
        // TODO Notify something?
    }
}
