package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.skills.RerollSource

class SetSkillRerollUsed(private val source: RerollSource) : Command {

    private var original: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        original = source.rerollUsed
        source.rerollUsed = true
    }

    override fun undo(state: Game, controller: GameController) {
        source.rerollUsed = original
    }
}
