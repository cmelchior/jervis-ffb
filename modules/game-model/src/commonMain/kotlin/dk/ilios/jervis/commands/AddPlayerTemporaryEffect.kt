package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.modifiers.TemporaryEffect

class AddPlayerTemporaryEffect(private val player: Player, val effect: TemporaryEffect) : Command {
    override fun execute(state: Game, controller: GameController) {
        player.temporaryEffects.add(effect)
        player.notifyUpdate()
    }

    override fun undo(state: Game, controller: GameController) {
        player.temporaryEffects.remove(effect)
        player.notifyUpdate()
    }
}
