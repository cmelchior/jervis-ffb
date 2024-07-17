package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Availability
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player

class SetPlayerAvailability(private val player: Player, val availability: Availability) : Command {
    private lateinit var originalAvailability: Availability
    override fun execute(state: Game, controller: GameController) {
        this.originalAvailability = player.available
        player.apply {
            available = availability
            notifyUpdate()
        }
    }

    override fun undo(state: Game, controller: GameController) {
        player.apply {
            available = originalAvailability
            notifyUpdate()
        }
    }
}
