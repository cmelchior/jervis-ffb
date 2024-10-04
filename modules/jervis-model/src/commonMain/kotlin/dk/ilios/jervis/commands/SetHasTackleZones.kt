package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetHasTackleZones(private val player: Player, private val hasTackleZones: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game, controller: GameController) {
        originalValue = player.hasTackleZones
        player.apply {
            this.hasTackleZones = this@SetHasTackleZones.hasTackleZones
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            this.hasTackleZones = originalValue
            notifyUpdate()
        }
    }
}
