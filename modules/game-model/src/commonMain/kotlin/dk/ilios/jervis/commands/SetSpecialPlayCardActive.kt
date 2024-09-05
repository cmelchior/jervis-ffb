package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.inducements.SpecialPlayCard
import dk.ilios.jervis.rules.skills.RerollSource

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
