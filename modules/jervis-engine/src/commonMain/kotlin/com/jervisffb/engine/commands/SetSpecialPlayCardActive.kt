package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.inducements.SpecialPlayCard

class SetSpecialPlayCardActive(private val card: SpecialPlayCard, val active: Boolean) : Command {
    private var original: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        original = card.isActive
        card.isActive
        // TODO Notify something?
    }

    override fun undo(state: Game, controller: GameController) {
        card.isActive = original
        // TODO Notify something?
    }
}
