package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class AddNigglingInjuries(val player: Player, val change: Int): Command {
    var originalValue: Int = 0
    override fun execute(state: Game, controller: GameController) {
        originalValue = player.nigglingInjuries
        player.apply {
            nigglingInjuries += change
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            nigglingInjuries = originalValue
            notifyUpdate()
        }
    }
}
