package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.modifiers.StatModifier

class RemovePlayerStatModifier(private val player: Player, val modifier: StatModifier) : Command {
    override fun execute(state: Game, controller: GameController) {
        player.apply {
            removeStatModifier(modifier)
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            addStatModifier(modifier)
            notifyUpdate()
        }
    }
}
