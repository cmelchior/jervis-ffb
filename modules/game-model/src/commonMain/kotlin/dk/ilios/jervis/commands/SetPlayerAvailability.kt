package dk.ilios.jervis.commands

import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Availability
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

class SetPlayerAvailability(private val player: Player, val availability: Availability) : Command {
    private lateinit var originalAvailability: Availability
    override fun execute(state: Game, controller: GameController) {
        this.originalAvailability = player.available
        player.available = availability
    }

    override fun undo(state: Game, controller: GameController) {
        player.available = originalAvailability
    }
}
